package com.dongzy.common.common.io.zip;

import com.gee4j.common.Validate;
import com.gee4j.common.collection.CollectionUtils;
import com.gee4j.common.io.PathUtils;
import com.gee4j.common.io.zip.core.ZipFile;
import com.gee4j.common.io.zip.exception.ZipException;
import com.gee4j.common.io.zip.model.FileHeader;
import com.gee4j.common.io.zip.model.ZipParameters;
import com.gee4j.common.io.zip.util.Zip4jConstants;
import com.gee4j.common.text.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户创建或者解压缩
 * 支持密码压缩和解压缩等高级功能
 */
public final class ZipUtil {

    private Charset charset = Charset.forName("GBK");
    private File file;

    /**
     * 根据传入的参数构造函数
     *
     * @param file 压缩文件对应的file对象
     */
    public ZipUtil(File file) {
        Validate.notNull(file, "zip的文件对象不能为空");
        this.file = file;
    }

    /**
     * 设置字符的编码
     *
     * @param charset 字符编码
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * 解压缩指定的压缩文件
     *
     * @return 解压缩的文件集合
     */
    public File[] unZip() throws ZipException {
        return unZip(file.getParentFile().getAbsolutePath());
    }

    /**
     * 解压缩指定的压缩文件
     *
     * @param unzipPath 解压缩的路径(如果不提供,那么解压缩在压缩文件所在的路径下面)
     * @return 解压缩的文件集合
     */
    public File[] unZip(String unzipPath) throws ZipException {
        return unZip(unzipPath, null);
    }

    /**
     * 解压缩指定的压缩文件
     *
     * @param unzipPath 解压缩的路径(如果不提供,那么解压缩在压缩文件所在的路径下面)
     * @param password  解压缩的密码
     * @return 解压缩的文件集合
     */
    public File[] unZip(String unzipPath, String password) throws ZipException {

        if (StringUtils.isBlank(password)) {
            //无密码解压缩
            try {
                return JdkZipUtils.unZip(file, unzipPath, charset);
            } catch (IOException e) {
                throw new ZipException(e);
            }
        } else {
            //支持有密码解压缩
            ZipFile zipFile = new ZipFile(file);
            zipFile.setFileNameCharset(charset.name());

            if (!zipFile.isValidZipFile()) {
                throw new ZipException("压缩文件不合法,可能被损坏.");
            }

            PathUtils.createDir(unzipPath);

            if (zipFile.isEncrypted()) {
                zipFile.setPassword(password.toCharArray());
            }
            zipFile.extractAll(unzipPath);

            List<FileHeader> headerList = zipFile.getFileHeaders();
            List<File> extractedFileList = new ArrayList<>();
            for (FileHeader fileHeader : headerList) {
                extractedFileList.add(new File(unzipPath, fileHeader.getFileName()));
            }
            return CollectionUtils.toArray(extractedFileList);
        }
    }

    /**
     * 将指定的文件或者目录集合打成压缩包
     *
     * @param path 被压缩的文件或目录
     */
    public void zip(String path) throws ZipException {
        zip(path, null, null);
    }

    /**
     * 将指定的文件或者目录集合打成压缩包
     *
     * @param path 被压缩的文件或目录
     */
    public void zip(String path, String password) throws ZipException {
        zip(path, null, password);
    }

    /**
     * 将指定的文件或者目录集合打成压缩包
     *
     * @param path 被压缩的文件或目录
     */
    public void zip(String path, String rootPath, String password) throws ZipException {
        Validate.notBlank(path, "被压缩的路径不能为空");
        Set<String> paths = new HashSet<>(1);
        paths.add(path);

        if (StringUtils.isBlank(rootPath)) {
            File file = new File(path);
            rootPath = (file.isDirectory()) ? path : file.getParentFile().getAbsolutePath();
        }

        zip(paths, rootPath, password);
    }

    /**
     * 将指定的文件或者目录集合打成压缩包
     *
     * @param paths 被压缩的文件或路径集合(完整路径)
     */
    public void zip(Set<String> paths) throws ZipException {
        zip(paths, null, null);
    }

    /**
     * 将指定的文件或者目录集合打成压缩包
     *
     * @param paths 被压缩的文件或路径集合(完整路径)
     */
    public void zip(Set<String> paths, String password) throws ZipException {
        zip(paths, null, password);
    }

    /**
     * 将指定的文件或者目录集合打成压缩包
     *
     * @param paths    被压缩的文件或路径集合(完整路径)
     * @param rootPath 所有被压缩文件的相对跟路径(完整路径)
     * @param password 压缩的密码
     */
    public void zip(Set<String> paths, String rootPath, String password) throws ZipException {

        try {
            ZipParameters parameters = initZipParameters(password);

            //第一步,获取所有需要压缩的文件(因为传入的可能既有文件,也有目录)
            Set<Path> fullPaths = new HashSet<>();        //因为文件可能重复,所以需要对路径做去重处理
            for (String path : paths) {
                Path subPath = Paths.get(path);
                if (subPath.toFile().isDirectory()) {
                    Collection<Path> subPaths = PathUtils.getFiles(subPath);
                    fullPaths.addAll(subPaths);
                } else {
                    fullPaths.add(subPath);
                }
            }

            if (parameters == null) {            //无密码压缩
                try {
                    JdkZipUtils.zip(fullPaths, rootPath, file, charset);
                } catch (IOException e) {
                    throw new ZipException(e);
                }
            } else {                             //有密码压缩
                parameters.setRootFolderInZip(rootPath);
                ZipFile zipFile = new ZipFile(file);
                zipFile.setFileNameCharset(charset.name());
                zipFile.addFiles(fullPaths.stream().map(Path::toFile).collect(Collectors.toList()), parameters);
            }
        } catch (IOException e) {
            throw new ZipException("文件访问异常", e);
        }
    }

    /**
     * 压缩输出输出流中的内容
     *
     * @param inputStream 输出流
     * @param fileName    文件名
     */
    public void zip(InputStream inputStream, String fileName) throws ZipException {
        zip(inputStream, fileName, null);
    }

    /**
     * 压缩输出流中的内容，并设置密码
     *
     * @param inputStream 输出流
     * @param fileName    文件名
     * @param password    解压缩的密码
     */
    public void zip(InputStream inputStream, String fileName, String password) throws ZipException {
        Validate.notNull(inputStream, "输入流不能为空");
        Validate.notBlank(fileName, "文件名不能为空");
        try {
            ZipParameters parameters = initZipParameters(password);

            if (parameters == null) {            //无密码压缩
                try {
                    JdkZipUtils.zip(inputStream, file, fileName, charset);
                } catch (IOException e) {
                    throw new ZipException(e);
                }
            } else {                             //有密码压缩
                parameters.setSourceExternalStream(true);
                parameters.setFileNameInZip(fileName);
                ZipFile zipFile = new ZipFile(file);
                zipFile.setFileNameCharset(charset.name());
                zipFile.addStream(inputStream, parameters);
            }
        } catch (IOException e) {
            throw new ZipException("文件访问异常", e);
        }
    }

    //初始化压缩前的准备
    private ZipParameters initZipParameters(String password) throws IOException {
        Validate.notNull(file, "保存的压缩文件不能为空!");

        if (file.exists()) {
            boolean delete = file.delete();
            if (!delete) {
                throw new IllegalStateException("无法删除已经存在的压缩文件");
            }
        }

        //检查目录是否已经存在,如果不存在,那么先创建改目录
        PathUtils.createFileDir(file);

        ZipParameters parameters = null;
        if (StringUtils.notBlank(password)) {
            parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);               // 压缩方式
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);        // 压缩级别
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);         // 加密方式
            parameters.setPassword(password.toCharArray());
        }

        return parameters;
    }
}
