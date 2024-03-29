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

package com.dongzy.common.common.io.zip.zip;

import com.dongzy.common.common.io.zip.exception.ZipException;
import com.dongzy.common.common.io.zip.io.SplitOutputStream;
import com.dongzy.common.common.io.zip.io.ZipOutputStream;
import com.dongzy.common.common.io.zip.model.EndCentralDirRecord;
import com.dongzy.common.common.io.zip.model.FileHeader;
import com.dongzy.common.common.io.zip.model.ZipModel;
import com.dongzy.common.common.io.zip.model.ZipParameters;
import com.dongzy.common.common.io.zip.progress.ProgressMonitor;
import com.dongzy.common.common.io.zip.util.*;
import com.dongzy.common.common.text.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ZipEngine {

    private ZipModel zipModel;

    public ZipEngine(ZipModel zipModel) throws ZipException {

        if (zipModel == null) {
            throw new ZipException("zip model is null in ZipEngine constructor");
        }

        this.zipModel = zipModel;
    }

    public void addFiles(final List fileList, final ZipParameters parameters,
                         final ProgressMonitor progressMonitor, boolean runInThread) throws ZipException {

        if (fileList == null || parameters == null) {
            throw new ZipException("one of the input parameters is null when adding files");
        }

        if (fileList.size() <= 0) {
            throw new ZipException("no files to add");
        }

        progressMonitor.setCurrentOperation(ProgressMonitor.OPERATION_ADD);
        progressMonitor.setState(ProgressMonitor.STATE_BUSY);
        progressMonitor.setResult(ProgressMonitor.RESULT_WORKING);

        if (runInThread) {
            progressMonitor.setTotalWork(calculateTotalWork(fileList, parameters));
            progressMonitor.setFileName(((File) fileList.get(0)).getAbsolutePath());

            Thread thread = new Thread(InternalZipConstants.THREAD_NAME) {
                public void run() {
                    try {
                        initAddFiles(fileList, parameters, progressMonitor);
                    } catch (ZipException e) {
                    }
                }
            };
            thread.start();

        } else {
            initAddFiles(fileList, parameters, progressMonitor);
        }
    }

    private void initAddFiles(List fileList, ZipParameters parameters,
                              ProgressMonitor progressMonitor) throws ZipException {

        if (fileList == null || parameters == null) {
            throw new ZipException("one of the input parameters is null when adding files");
        }

        if (fileList.size() <= 0) {
            throw new ZipException("no files to add");
        }

        if (zipModel.getEndCentralDirRecord() == null) {
            zipModel.setEndCentralDirRecord(createEndOfCentralDirectoryRecord());
        }

        ZipOutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            checkParameters(parameters);

            removeFilesIfExists(fileList, parameters, progressMonitor);

            boolean isZipFileAlreadExists = Zip4jUtil.checkFileExists(zipModel.getZipFile());

            SplitOutputStream splitOutputStream = new SplitOutputStream(new File(zipModel.getZipFile()), zipModel.getSplitLength());
            outputStream = new ZipOutputStream(splitOutputStream, this.zipModel);

            if (isZipFileAlreadExists) {
                if (zipModel.getEndCentralDirRecord() == null) {
                    throw new ZipException("invalid end of central directory record");
                }
                splitOutputStream.seek(zipModel.getEndCentralDirRecord().getOffsetOfStartOfCentralDir());
            }
            byte[] readBuff = new byte[InternalZipConstants.BUFF_SIZE];
            int readLen;
            for (Object aFileList : fileList) {

                if (progressMonitor.isCancelAllTasks()) {
                    progressMonitor.setResult(ProgressMonitor.RESULT_CANCELLED);
                    progressMonitor.setState(ProgressMonitor.STATE_READY);
                    return;
                }

                ZipParameters fileParameters = (ZipParameters) parameters.clone();

                progressMonitor.setFileName(((File) aFileList).getAbsolutePath());

                if (!((File) aFileList).isDirectory()) {
                    if (fileParameters.isEncryptFiles() && fileParameters.getEncryptionMethod() == Zip4jConstants.ENC_METHOD_STANDARD) {
                        progressMonitor.setCurrentOperation(ProgressMonitor.OPERATION_CALC_CRC);
                        fileParameters.setSourceFileCRC((int) CRCUtil.computeFileCRC(((File) aFileList).getAbsolutePath(), progressMonitor));
                        progressMonitor.setCurrentOperation(ProgressMonitor.OPERATION_ADD);

                        if (progressMonitor.isCancelAllTasks()) {
                            progressMonitor.setResult(ProgressMonitor.RESULT_CANCELLED);
                            progressMonitor.setState(ProgressMonitor.STATE_READY);
                            return;
                        }
                    }

                    if (Zip4jUtil.getFileLengh((File) aFileList) == 0) {
                        fileParameters.setCompressionMethod(Zip4jConstants.COMP_STORE);
                    }
                }

                outputStream.putNextEntry((File) aFileList, fileParameters);
                if (((File) aFileList).isDirectory()) {
                    outputStream.closeEntry();
                    continue;
                }

                inputStream = new FileInputStream((File) aFileList);

                while ((readLen = inputStream.read(readBuff)) != -1) {
                    if (progressMonitor.isCancelAllTasks()) {
                        progressMonitor.setResult(ProgressMonitor.RESULT_CANCELLED);
                        progressMonitor.setState(ProgressMonitor.STATE_READY);
                        return;
                    }

                    outputStream.write(readBuff, 0, readLen);
                    progressMonitor.updateWorkCompleted(readLen);
                }

                outputStream.closeEntry();

                inputStream.close();
            }

            outputStream.finish();
            progressMonitor.endProgressMonitorSuccess();
        } catch (ZipException e) {
            progressMonitor.endProgressMonitorError(e);
            throw e;
        } catch (Exception e) {
            progressMonitor.endProgressMonitorError(e);
            throw new ZipException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public void addStreamToZip(InputStream inputStream, ZipParameters parameters) throws ZipException {
        if (inputStream == null || parameters == null) {
            throw new ZipException("one of the input parameters is null, cannot add stream to zip");
        }

        ZipOutputStream outputStream = null;

        try {
            checkParameters(parameters);

            boolean isZipFileAlreadExists = Zip4jUtil.checkFileExists(zipModel.getZipFile());

            SplitOutputStream splitOutputStream = new SplitOutputStream(new File(zipModel.getZipFile()), zipModel.getSplitLength());
            outputStream = new ZipOutputStream(splitOutputStream, this.zipModel);

            if (isZipFileAlreadExists) {
                if (zipModel.getEndCentralDirRecord() == null) {
                    throw new ZipException("invalid end of central directory record");
                }
                splitOutputStream.seek(zipModel.getEndCentralDirRecord().getOffsetOfStartOfCentralDir());
            }

            byte[] readBuff = new byte[InternalZipConstants.BUFF_SIZE];
            int readLen = -1;

            outputStream.putNextEntry(null, parameters);

            if (!parameters.getFileNameInZip().endsWith("/") &&
                    !parameters.getFileNameInZip().endsWith("\\")) {
                while ((readLen = inputStream.read(readBuff)) != -1) {
                    outputStream.write(readBuff, 0, readLen);
                }
            }

            outputStream.closeEntry();
            outputStream.finish();

        } catch (ZipException e) {
            throw e;
        } catch (Exception e) {
            throw new ZipException(e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public void addFolderToZip(File file, ZipParameters parameters,
                               ProgressMonitor progressMonitor, boolean runInThread) throws ZipException {
        if (file == null || parameters == null) {
            throw new ZipException("one of the input parameters is null, cannot add folder to zip");
        }

        if (!Zip4jUtil.checkFileExists(file.getAbsolutePath())) {
            throw new ZipException("input folder does not exist");
        }

        if (!file.isDirectory()) {
            throw new ZipException("input file is not a folder, user addFileToZip method to add files");
        }

        if (!Zip4jUtil.checkFileReadAccess(file.getAbsolutePath())) {
            throw new ZipException("cannot read folder: " + file.getAbsolutePath());
        }

        String rootFolderPath = null;
        if (parameters.isIncludeRootFolder()) {
            if (file.getAbsolutePath() != null) {
                rootFolderPath = file.getAbsoluteFile().getParentFile() != null ? file.getAbsoluteFile().getParentFile().getAbsolutePath() : "";
            } else {
                rootFolderPath = file.getParentFile() != null ? file.getParentFile().getAbsolutePath() : "";
            }
        } else {
            rootFolderPath = file.getAbsolutePath();
        }

        parameters.setDefaultFolderPath(rootFolderPath);

        ArrayList fileList = Zip4jUtil.getFilesInDirectoryRec(file, parameters.isReadHiddenFiles());

        if (parameters.isIncludeRootFolder()) {
            if (fileList == null) {
                fileList = new ArrayList();
            }
            fileList.add(file);
        }

        addFiles(fileList, parameters, progressMonitor, runInThread);
    }


    private void checkParameters(ZipParameters parameters) throws ZipException {

        if (parameters == null) {
            throw new ZipException("cannot validate zip parameters");
        }

        if ((parameters.getCompressionMethod() != Zip4jConstants.COMP_STORE) &&
                parameters.getCompressionMethod() != Zip4jConstants.COMP_DEFLATE) {
            throw new ZipException("unsupported compression type");
        }

        if (parameters.getCompressionMethod() == Zip4jConstants.COMP_DEFLATE) {
            if (parameters.getCompressionLevel() < 0 && parameters.getCompressionLevel() > 9) {
                throw new ZipException("invalid compression level. compression level dor deflate should be in the range of 0-9");
            }
        }

        if (parameters.isEncryptFiles()) {
            if (parameters.getEncryptionMethod() != Zip4jConstants.ENC_METHOD_STANDARD &&
                    parameters.getEncryptionMethod() != Zip4jConstants.ENC_METHOD_AES) {
                throw new ZipException("unsupported encryption method");
            }

            if (parameters.getPassword() == null || parameters.getPassword().length <= 0) {
                throw new ZipException("input password is empty or null");
            }
        } else {
            parameters.setAesKeyStrength(-1);
            parameters.setEncryptionMethod(-1);
        }

    }

    /**
     * Before adding a file to a zip file, we check if a file already exists in the zip file
     * with the same fileName (including path, if exists). If yes, then we remove this file
     * before adding the file<br><br>
     *
     * <b>Note:</b> Relative path has to be passed as the fileName
     *
     * @param parameters
     * @param fileList
     * @throws ZipException
     */
    private void removeFilesIfExists(List fileList, ZipParameters parameters, ProgressMonitor progressMonitor) throws ZipException {

        if (zipModel == null || zipModel.getCentralDirectory() == null ||
                zipModel.getCentralDirectory().getFileHeaders() == null ||
                zipModel.getCentralDirectory().getFileHeaders().size() <= 0) {
            //For a new zip file, this condition satisfies, so do nothing
            return;
        }
        RandomAccessFile outputStream = null;

        try {
            for (int i = 0; i < fileList.size(); i++) {
                File file = (File) fileList.get(i);

                String fileName = Zip4jUtil.getRelativeFileName(file.getAbsolutePath(),
                        parameters.getRootFolderInZip(), parameters.getDefaultFolderPath());

                FileHeader fileHeader = Zip4jUtil.getFileHeader(zipModel, fileName);
                if (fileHeader != null) {

                    if (outputStream != null) {
                        outputStream.close();
                        outputStream = null;
                    }

                    ArchiveMaintainer archiveMaintainer = new ArchiveMaintainer();
                    progressMonitor.setCurrentOperation(ProgressMonitor.OPERATION_REMOVE);
                    HashMap retMap = archiveMaintainer.initRemoveZipFile(zipModel,
                            fileHeader, progressMonitor);

                    if (progressMonitor.isCancelAllTasks()) {
                        progressMonitor.setResult(ProgressMonitor.RESULT_CANCELLED);
                        progressMonitor.setState(ProgressMonitor.STATE_READY);
                        return;
                    }

                    progressMonitor
                            .setCurrentOperation(ProgressMonitor.OPERATION_ADD);

                    if (outputStream == null) {
                        outputStream = prepareFileOutputStream();

                        if (retMap != null) {
                            if (retMap.get(InternalZipConstants.OFFSET_CENTRAL_DIR) != null) {
                                long offsetCentralDir = -1;
                                try {
                                    offsetCentralDir = Long
                                            .parseLong((String) retMap
                                                    .get(InternalZipConstants.OFFSET_CENTRAL_DIR));
                                } catch (NumberFormatException e) {
                                    throw new ZipException(
                                            "NumberFormatException while parsing offset central directory. " +
                                                    "Cannot update already existing file header");
                                } catch (Exception e) {
                                    throw new ZipException(
                                            "Error while parsing offset central directory. " +
                                                    "Cannot update already existing file header");
                                }

                                if (offsetCentralDir >= 0) {
                                    outputStream.seek(offsetCentralDir);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ZipException(e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }

    private RandomAccessFile prepareFileOutputStream() throws ZipException {
        String outPath = zipModel.getZipFile();
        if (StringUtils.isBlank(outPath)) {
            throw new ZipException("invalid output path");
        }

        try {
            File outFile = new File(outPath);
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            return new RandomAccessFile(outFile, InternalZipConstants.WRITE_MODE);
        } catch (FileNotFoundException e) {
            throw new ZipException(e);
        }
    }

    private EndCentralDirRecord createEndOfCentralDirectoryRecord() {
        EndCentralDirRecord endCentralDirRecord = new EndCentralDirRecord();
        endCentralDirRecord.setSignature(InternalZipConstants.ENDSIG);
        endCentralDirRecord.setNoOfThisDisk(0);
        endCentralDirRecord.setTotNoOfEntriesInCentralDir(0);
        endCentralDirRecord.setTotNoOfEntriesInCentralDirOnThisDisk(0);
        endCentralDirRecord.setOffsetOfStartOfCentralDir(0);
        return endCentralDirRecord;
    }

    private long calculateTotalWork(List fileList, ZipParameters parameters) throws ZipException {
        if (fileList == null) {
            throw new ZipException("file list is null, cannot calculate total work");
        }

        long totalWork = 0;

        for (int i = 0; i < fileList.size(); i++) {
            if (fileList.get(i) instanceof File) {
                if (((File) fileList.get(i)).exists()) {
                    if (parameters.isEncryptFiles() &&
                            parameters.getEncryptionMethod() == Zip4jConstants.ENC_METHOD_STANDARD) {
                        totalWork += (Zip4jUtil.getFileLengh((File) fileList.get(i)) * 2);
                    } else {
                        totalWork += Zip4jUtil.getFileLengh((File) fileList.get(i));
                    }

                    if (zipModel.getCentralDirectory() != null &&
                            zipModel.getCentralDirectory().getFileHeaders() != null &&
                            zipModel.getCentralDirectory().getFileHeaders().size() > 0) {
                        String relativeFileName = Zip4jUtil.getRelativeFileName(
                                ((File) fileList.get(i)).getAbsolutePath(), parameters.getRootFolderInZip(), parameters.getDefaultFolderPath());
                        FileHeader fileHeader = Zip4jUtil.getFileHeader(zipModel, relativeFileName);
                        if (fileHeader != null) {
                            totalWork += (Zip4jUtil.getFileLengh(new File(zipModel.getZipFile())) - fileHeader.getCompressedSize());
                        }
                    }
                }
            }
        }

        return totalWork;
    }
}
