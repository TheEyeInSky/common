package com.dongzy.common.common.io.zip;

import com.dongzy.common.common.collection.CollectionUtils;
import com.dongzy.common.common.io.PathUtils;
import com.dongzy.common.common.text.StringUtils;
import com.dongzy.common.log.TextLoggerFactory;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * JDK自带的用户创建或者解压缩
 * 不支持密码压缩等高级特性
 * 不直接对外暴漏本类
 */
final class JdkZipUtils {

    private final static Logger LOGGER = TextLoggerFactory.getInstance().getLogger(JdkZipUtils.class);
    private static final int BUFFER_SIZE = 4096;

    /**
     * 解压缩指定的压缩文件
     *
     * @param file      被解压缩的完整路径名称
     * @param charset   解压缩的编码
     * @param unzipPath 解压缩的路径(如果不提供,那么解压缩在压缩文件所在的路径下面)
     */
    static File[] unZip(File file, String unzipPath, Charset charset) throws IOException {

        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        Collection<File> files = new ArrayList<>();

        try (ZipFile zipFile = new ZipFile(file, charset)) {
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                try {
                    byte[] buf = new byte[BUFFER_SIZE];
                    ZipEntry entry = (ZipEntry) entries.nextElement();
                    String filename = entry.getName();
                    file = new File(PathUtils.joinPath(unzipPath, filename));
                    PathUtils.createFileDir(file);
                    files.add(file);
                    if (entry.isDirectory()) {
                        continue;
                    }

                    if (file.createNewFile()) {
                        inputStream = zipFile.getInputStream(entry);
                        fileOutputStream = new FileOutputStream(file);
                        bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER_SIZE);
                        int len;
                        while ((len = inputStream.read(buf)) != -1) {
                            bufferedOutputStream.write(buf, 0, len);
                        }
                        bufferedOutputStream.flush();
                        bufferedOutputStream.close();
                        inputStream.close();
                    } else {
                        throw new IOException("文件" + file + "创建失败！");
                    }
                } catch (IOException ex) {
                    LOGGER.error("解压缩文件时发生错误!", ex);
                    throw ex;
                } finally {
                    if (bufferedOutputStream != null) {
                        bufferedOutputStream.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            }
        }

        return CollectionUtils.toArray(files);
    }

    static void zip(Collection<Path> paths, String rootPath, File zipFile, Charset charset) throws IOException {

        //检查目录是否已经存在,如果不存在,那么先创建改目录
        PathUtils.createFileDir(zipFile);

        byte[] buf = new byte[BUFFER_SIZE];

        //对文件集合进行压缩处理
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile), charset)) {
            for (Path path : paths) {
                if (Files.isDirectory(path)) {
                    continue;
                }

                File file = path.toFile();
                try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file))) {
                    //如果没有根路径，那么采用完整路径，否则采用相对路径
                    String name = StringUtils.isBlank(rootPath) ? path.toString() : PathUtils.getRelativePath(rootPath, path.toString());
                    zipOut.putNextEntry(new ZipEntry(name));
                    int len;
                    while ((len = input.read(buf)) != -1) {
                        zipOut.write(buf, 0, len);
                    }
                } catch (IOException e) {
                    LOGGER.error("添加压缩文件时发生异常!", e);
                    throw e;
                }
            }
        }
    }

    static void zip(InputStream inputStream, File zipFile, String fileName, Charset charset) throws IOException {
        //检查目录是否已经存在,如果不存在,那么先创建改目录
        PathUtils.createFileDir(zipFile);

        byte[] buf = new byte[BUFFER_SIZE];

        //对文件集合进行压缩处理
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile), charset)) {
            //采用相对路径
            zipOut.putNextEntry(new ZipEntry(fileName));
            int len;
            while ((len = inputStream.read(buf)) != -1) {
                zipOut.write(buf, 0, len);
            }
        }
    }

}
