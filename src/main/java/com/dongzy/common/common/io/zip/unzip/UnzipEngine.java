/*
 * Copyright 2010 Srikanth Reddy Lingala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dongzy.common.common.io.zip.unzip;

import com.dongzy.common.common.io.PathUtils;
import com.dongzy.common.common.io.zip.core.HeaderReader;
import com.dongzy.common.common.io.zip.crypto.AESDecrypter;
import com.dongzy.common.common.io.zip.crypto.IDecrypter;
import com.dongzy.common.common.io.zip.crypto.StandardDecrypter;
import com.dongzy.common.common.io.zip.exception.ZipException;
import com.dongzy.common.common.io.zip.io.InflaterInputStream;
import com.dongzy.common.common.io.zip.io.PartInputStream;
import com.dongzy.common.common.io.zip.io.ZipInputStream;
import com.dongzy.common.common.io.zip.model.*;
import com.dongzy.common.common.io.zip.progress.ProgressMonitor;
import com.dongzy.common.common.io.zip.util.InternalZipConstants;
import com.dongzy.common.common.io.zip.util.Raw;
import com.dongzy.common.common.io.zip.util.Zip4jConstants;
import com.dongzy.common.common.io.zip.util.Zip4jUtil;
import com.dongzy.common.common.text.StringUtils;

import java.io.*;
import java.util.Arrays;
import java.util.zip.CRC32;

public class UnzipEngine {

    private ZipModel zipModel;
    private FileHeader fileHeader;
    private int currSplitFileCounter = 0;
    private LocalFileHeader localFileHeader;
    private com.dongzy.common.common.io.zip.crypto.IDecrypter decrypter;
    private CRC32 crc;

    public UnzipEngine(ZipModel zipModel, FileHeader fileHeader) throws ZipException {
        if (zipModel == null || fileHeader == null) {
            throw new ZipException("Invalid parameters passed to StoreUnzip. One or more of the parameters were null");
        }

        this.zipModel = zipModel;
        this.fileHeader = fileHeader;
        this.crc = new CRC32();
    }

    public void unzipFile(ProgressMonitor progressMonitor,
                          String outPath, String newFileName, UnzipParameters unzipParameters) throws ZipException {
        if (zipModel == null || fileHeader == null || StringUtils.isBlank(outPath)) {
            throw new ZipException("Invalid parameters passed during unzipping file. One or more of the parameters were null");
        }
        InputStream is = null;
        OutputStream os = null;
        try {
            byte[] buff = new byte[InternalZipConstants.BUFF_SIZE];
            int readLength;

            is = getInputStream();
            os = getOutputStream(outPath, newFileName);

            while ((readLength = is.read(buff)) != -1) {
                os.write(buff, 0, readLength);
                progressMonitor.updateWorkCompleted(readLength);
                if (progressMonitor.isCancelAllTasks()) {
                    progressMonitor.setResult(ProgressMonitor.RESULT_CANCELLED);
                    progressMonitor.setState(ProgressMonitor.STATE_READY);
                    return;
                }
            }

            closeStreams(is, os);

            UnzipUtil.applyFileAttributes(fileHeader, new File(getOutputFileNameWithPath(outPath, newFileName)), unzipParameters);

        } catch (Exception e) {
            throw new ZipException(e);
        } finally {
            closeStreams(is, os);
        }
    }

    public ZipInputStream getInputStream() throws ZipException {
        if (fileHeader == null) {
            throw new ZipException("file header is null, cannot get inputstream");
        }

        RandomAccessFile raf = null;
        try {
            raf = createFileHandler(InternalZipConstants.READ_MODE);
            String errMsg = "local header and file header do not match";
            //checkSplitFile();

            if (!checkLocalHeader())
                throw new ZipException(errMsg);

            init(raf);

            long comprSize = localFileHeader.getCompressedSize();
            long offsetStartOfData = localFileHeader.getOffsetStartOfData();

            if (localFileHeader.isEncrypted()) {
                if (localFileHeader.getEncryptionMethod() == Zip4jConstants.ENC_METHOD_AES) {
                    if (decrypter instanceof AESDecrypter) {
                        comprSize -= (((AESDecrypter) decrypter).getSaltLength() +
                                ((AESDecrypter) decrypter).getPasswordVerifierLength() + 10);
                        offsetStartOfData += (((AESDecrypter) decrypter).getSaltLength() +
                                ((AESDecrypter) decrypter).getPasswordVerifierLength());
                    } else {
                        throw new ZipException("invalid decryptor when trying to calculate " +
                                "compressed size for AES encrypted file: " + fileHeader.getFileName());
                    }
                } else if (localFileHeader.getEncryptionMethod() == Zip4jConstants.ENC_METHOD_STANDARD) {
                    comprSize -= InternalZipConstants.STD_DEC_HDR_SIZE;
                    offsetStartOfData += InternalZipConstants.STD_DEC_HDR_SIZE;
                }
            }

            int compressionMethod = fileHeader.getCompressionMethod();
            if (fileHeader.getEncryptionMethod() == Zip4jConstants.ENC_METHOD_AES) {
                if (fileHeader.getAesExtraDataRecord() != null) {
                    compressionMethod = fileHeader.getAesExtraDataRecord().getCompressionMethod();
                } else {
                    throw new ZipException("AESExtraDataRecord does not exist for AES encrypted file: " + fileHeader.getFileName());
                }
            }
            raf.seek(offsetStartOfData);
            switch (compressionMethod) {
                case Zip4jConstants.COMP_STORE:
                    return new ZipInputStream(new PartInputStream(raf, offsetStartOfData, comprSize, this));
                case Zip4jConstants.COMP_DEFLATE:
                    return new ZipInputStream(new InflaterInputStream(raf, offsetStartOfData, comprSize, this));
                default:
                    throw new ZipException("compression type not supported");
            }
        } catch (ZipException e) {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e1) {
                    //ignore
                }
            }
            throw e;
        } catch (Exception e) {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException ignored) {
                }
            }
            throw new ZipException(e);
        }

    }

    private void init(RandomAccessFile raf) throws ZipException {

        if (localFileHeader == null) {
            throw new ZipException("local file header is null, cannot initialize input stream");
        }

        try {
            initDecrypter(raf);
        } catch (ZipException e) {
            throw e;
        } catch (Exception e) {
            throw new ZipException(e);
        }
    }

    private void initDecrypter(RandomAccessFile raf) throws ZipException {
        if (localFileHeader == null) {
            throw new ZipException("local file header is null, cannot init decrypter");
        }

        if (localFileHeader.isEncrypted()) {
            if (localFileHeader.getEncryptionMethod() == Zip4jConstants.ENC_METHOD_STANDARD) {
                decrypter = new StandardDecrypter(fileHeader, getStandardDecrypterHeaderBytes(raf));
            } else if (localFileHeader.getEncryptionMethod() == Zip4jConstants.ENC_METHOD_AES) {
                decrypter = new AESDecrypter(localFileHeader, getAESSalt(raf), getAESPasswordVerifier(raf));
            } else {
                throw new ZipException("unsupported encryption method");
            }
        }
    }

    private byte[] getStandardDecrypterHeaderBytes(RandomAccessFile raf) throws ZipException {
        try {
            byte[] headerBytes = new byte[InternalZipConstants.STD_DEC_HDR_SIZE];
            raf.seek(localFileHeader.getOffsetStartOfData());
            raf.read(headerBytes, 0, 12);
            return headerBytes;
        } catch (Exception e) {
            throw new ZipException(e);
        }
    }

    private byte[] getAESSalt(RandomAccessFile raf) throws ZipException {
        if (localFileHeader.getAesExtraDataRecord() == null)
            return null;

        try {
            AESExtraDataRecord aesExtraDataRecord = localFileHeader.getAesExtraDataRecord();
            byte[] saltBytes = new byte[calculateAESSaltLength(aesExtraDataRecord)];
            raf.seek(localFileHeader.getOffsetStartOfData());
            raf.read(saltBytes);
            return saltBytes;
        } catch (IOException e) {
            throw new ZipException(e);
        }
    }

    private byte[] getAESPasswordVerifier(RandomAccessFile raf) throws ZipException {
        try {
            byte[] pvBytes = new byte[2];
            raf.read(pvBytes);
            return pvBytes;
        } catch (IOException e) {
            throw new ZipException(e);
        }
    }

    private int calculateAESSaltLength(AESExtraDataRecord aesExtraDataRecord) throws ZipException {
        if (aesExtraDataRecord == null) {
            throw new ZipException("unable to determine salt length: AESExtraDataRecord is null");
        }
        switch (aesExtraDataRecord.getAesStrength()) {
            case Zip4jConstants.AES_STRENGTH_128:
                return 8;
            case Zip4jConstants.AES_STRENGTH_192:
                return 12;
            case Zip4jConstants.AES_STRENGTH_256:
                return 16;
            default:
                throw new ZipException("unable to determine salt length: invalid aes key strength");
        }
    }

    public void checkCRC() throws ZipException {
        if (fileHeader != null) {
            if (fileHeader.getEncryptionMethod() == Zip4jConstants.ENC_METHOD_AES) {
                if (decrypter != null && decrypter instanceof AESDecrypter) {
                    byte[] tmpMacBytes = ((AESDecrypter) decrypter).getCalculatedAuthenticationBytes();
                    byte[] storedMac = ((AESDecrypter) decrypter).getStoredMac();
                    byte[] calculatedMac = new byte[InternalZipConstants.AES_AUTH_LENGTH];

                    if (calculatedMac == null || storedMac == null) {
                        throw new ZipException("CRC (MAC) check failed for " + fileHeader.getFileName());
                    }

                    System.arraycopy(tmpMacBytes, 0, calculatedMac, 0, InternalZipConstants.AES_AUTH_LENGTH);

                    if (!Arrays.equals(calculatedMac, storedMac)) {
                        throw new ZipException("invalid CRC (MAC) for file: " + fileHeader.getFileName());
                    }
                }
            } else {
                long calculatedCRC = crc.getValue() & 0xffffffffL;
                if (calculatedCRC != fileHeader.getCrc32()) {
                    String errMsg = "invalid CRC for file: " + fileHeader.getFileName();
                    if (localFileHeader.isEncrypted() &&
                            localFileHeader.getEncryptionMethod() == Zip4jConstants.ENC_METHOD_STANDARD) {
                        errMsg += " - Wrong Password?";
                    }
                    throw new ZipException(errMsg);
                }
            }
        }
    }

    private boolean checkLocalHeader() throws ZipException {
        RandomAccessFile rafForLH = null;
        try {
            rafForLH = checkSplitFile();

            if (rafForLH == null) {
                rafForLH = new RandomAccessFile(new File(this.zipModel.getZipFile()), InternalZipConstants.READ_MODE);
            }

            HeaderReader headerReader = new HeaderReader(rafForLH);
            this.localFileHeader = headerReader.readLocalFileHeader(fileHeader);

            if (localFileHeader == null) {
                throw new ZipException("error reading local file header. Is this a valid zip file?");
            }

            //TODO Add more comparision later
            if (localFileHeader.getCompressionMethod() != fileHeader.getCompressionMethod()) {
                return false;
            }

            return true;
        } catch (FileNotFoundException e) {
            throw new ZipException(e);
        } finally {
            if (rafForLH != null) {
                try {
                    rafForLH.close();
                } catch (Exception e) {
                    //Ignore this
                }
            }
        }
    }

    private RandomAccessFile checkSplitFile() throws ZipException {
        if (zipModel.isSplitArchive()) {
            int diskNumberStartOfFile = fileHeader.getDiskNumberStart();
            currSplitFileCounter = diskNumberStartOfFile + 1;
            String curZipFile = zipModel.getZipFile();
            String partFile = null;
            if (diskNumberStartOfFile == zipModel.getEndCentralDirRecord().getNoOfThisDisk()) {
                partFile = zipModel.getZipFile();
            } else {
                if (diskNumberStartOfFile >= 9) {
                    partFile = curZipFile.substring(0, curZipFile.lastIndexOf(".")) + ".z" + (diskNumberStartOfFile + 1);
                } else {
                    partFile = curZipFile.substring(0, curZipFile.lastIndexOf(".")) + ".z0" + (diskNumberStartOfFile + 1);
                }
            }

            try {
                RandomAccessFile raf = new RandomAccessFile(partFile, InternalZipConstants.READ_MODE);

                if (currSplitFileCounter == 1) {
                    byte[] splitSig = new byte[4];
                    raf.read(splitSig);
                    if (Raw.readIntLittleEndian(splitSig, 0) != InternalZipConstants.SPLITSIG) {
                        throw new ZipException("invalid first part split file signature");
                    }
                }
                return raf;
            } catch (IOException e) {
                throw new ZipException(e);
            }
        }
        return null;
    }

    private RandomAccessFile createFileHandler(String mode) throws ZipException {
        if (this.zipModel == null || StringUtils.isBlank(this.zipModel.getZipFile())) {
            throw new ZipException("input parameter is null in getFilePointer");
        }

        try {
            RandomAccessFile raf;
            if (zipModel.isSplitArchive()) {
                raf = checkSplitFile();
            } else {
                raf = new RandomAccessFile(new File(this.zipModel.getZipFile()), mode);
            }
            return raf;
        } catch (Exception e) {
            throw new ZipException(e);
        }
    }

    private FileOutputStream getOutputStream(String outPath, String newFileName) throws ZipException {
        if (StringUtils.isBlank(outPath)) {
            throw new ZipException("invalid output path");
        }

        try {
            File file = new File(getOutputFileNameWithPath(outPath, newFileName));

            PathUtils.createFileDir(file);

            return new FileOutputStream(file);
        } catch (IOException e) {
            throw new ZipException(e);
        }
    }

    private String getOutputFileNameWithPath(String outPath, String newFileName) throws ZipException {
        String fileName;
        if (StringUtils.notBlank(newFileName)) {
            fileName = newFileName;
        } else {
            fileName = fileHeader.getFileName();
        }
        return PathUtils.joinPath(outPath, fileName);
    }

    public RandomAccessFile startNextSplitFile() throws IOException, FileNotFoundException {
        String currZipFile = zipModel.getZipFile();
        String partFile;
        if (currSplitFileCounter == zipModel.getEndCentralDirRecord().getNoOfThisDisk()) {
            partFile = zipModel.getZipFile();
        } else {
            if (currSplitFileCounter >= 9) {
                partFile = currZipFile.substring(0, currZipFile.lastIndexOf(".")) + ".z" + (currSplitFileCounter + 1);
            } else {
                partFile = currZipFile.substring(0, currZipFile.lastIndexOf(".")) + ".z0" + (currSplitFileCounter + 1);
            }
        }
        currSplitFileCounter++;
        try {
            if (!Zip4jUtil.checkFileExists(partFile)) {
                throw new IOException("zip split file does not exist: " + partFile);
            }
        } catch (ZipException e) {
            throw new IOException(e.getMessage());
        }
        return new RandomAccessFile(partFile, InternalZipConstants.READ_MODE);
    }

    private void closeStreams(InputStream is, OutputStream os) throws ZipException {
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException e) {
            if (e.getMessage().contains(" - Wrong Password?")) {
                throw new ZipException(e.getMessage());
            }
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                //do nothing
            }
        }
    }

    public void updateCRC(int b) {
        crc.update(b);
    }

    public void updateCRC(byte[] buff, int offset, int len) {
        if (buff != null) {
            crc.update(buff, offset, len);
        }
    }

    public FileHeader getFileHeader() {
        return fileHeader;
    }

    public IDecrypter getDecrypter() {
        return decrypter;
    }

    public ZipModel getZipModel() {
        return zipModel;
    }

    public LocalFileHeader getLocalFileHeader() {
        return localFileHeader;
    }
}
