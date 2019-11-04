package com.dongzy.common.common.io;

import com.dongzy.common.common.text.StringUtils;
import com.dongzy.common.common.SystemUtils;
import com.dongzy.common.common.Validate;
import com.dongzy.common.common.text.StringUtils;
import com.dongzy.common.config.FileConfigReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 清空路径下的文件
 * 1、清除所有的子目录和文件
 * 2、清除符合特定表达式的子目录和文件
 * 3、是否删除当前目录（根目录除外）
 *
 * @author 勇
 */
public final class PathUtils {

    /**
     * 路径拆分的字符
     */
    public final static char[] SPLITS_URL_PATH = new char[]{'/', '\\'};

    /**
     * 采用路径的左斜杠分隔符，将多个字符串连接起来，连接的时候会自动把右斜杠替换成左斜杠，
     * 每一个路径单元之间也会用左斜杠连接起来，
     * 如果被连接的路径中已经包含了左斜杠或者右斜杠，那么系统会自动进行处理，以保证最终连接路径字符串的正确性。
     *
     * @param originalPath 最初原路径
     * @param paths        需要被添加的路径
     * @return 连接好的路径
     */
    public static String joinPath(String originalPath, String... paths) {
        List<String> strPaths = new ArrayList<>();
        strPaths.add(StringUtils.trimRight(originalPath.trim(), SPLITS_URL_PATH).replace('/', File.separatorChar)
                .replace('\\', File.separatorChar));
        for (String path : paths) {
            if (path == null) continue;

            String newPath = StringUtils.trim(path.trim(), SPLITS_URL_PATH).replace('/', File.separatorChar)
                    .replace('\\', File.separatorChar);
            if (StringUtils.notBlank(newPath))
                strPaths.add(newPath);
        }

        String fullPath = StringUtils.join(File.separatorChar + StringUtils.EMPTY, strPaths);

        //如果最后一个路径包含文件分隔符，那么就添加一个结尾的路径分隔符
        String lastPath = paths[paths.length - 1].trim();
        if (lastPath.endsWith("\\") || lastPath.endsWith("/")) {
            fullPath += File.separatorChar;
        }

        return fullPath;
    }

    /**
     * 获取指定目录下的子目录和文件
     *
     * @param rootPath 获取文件的根路径
     * @return 该路径下的所有文件的列表
     * @throws IOException 异常类
     */
    public static Collection<Path> getFiles(Path rootPath) throws IOException {
        return getFiles(rootPath, StringUtils.EMPTY, true);
    }

    /**
     * 获取指定路径下面的指定文件
     *
     * @param rootPath 需要搜索的目录
     * @param fileName 需要查找的文件
     * @return 找到的path对象，如果没有找到，那么返回null
     */
    public static Path getFile(Path rootPath, String fileName) throws IOException {
        List<Path> paths = getFiles(rootPath, fileName, false);
        if (paths.size() == 0) {
            return null;
        } else {
            return paths.get(0);
        }
    }

    /**
     * 获取指定目录下的子目录和文件
     *
     * @param rootPath  获取文件的根路径
     * @param glob      文件过滤表达式
     * @param recursive 是否包含子目录
     * @return 该路径下所有符合过滤表达式的文件列表
     * @throws IOException 异常类
     */
    public static List<Path> getFiles(Path rootPath, String glob, boolean recursive) throws IOException {
        Validate.notNull(rootPath);
        Validate.notNull(glob);

        LinkedList<Path> pathCollection = new LinkedList<>();
        glob = glob.toLowerCase().trim();
        if (glob.equals("*") || glob.equals("*.*")) {           //如果顾虑符号为*或*.*，那么就将过滤符号替换成空
            glob = StringUtils.EMPTY;
        }
        Pattern pattern = null;
        if (StringUtils.notBlank(glob)) {
            glob = StringUtils.replace(glob, ".", "\\.");
            glob = StringUtils.replace(glob, "*", "(.*)?");
            pattern = Pattern.compile(glob);
        }
        getFiles(rootPath, pattern, recursive, pathCollection);
        return pathCollection;
    }

    // 内部方法
    private static void getFiles(final Path rootPath, final Pattern pattern, final boolean recursive, final LinkedList<Path> pathCollection) throws IOException {
        if (!Files.exists(rootPath)) {
            return;
        }

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(rootPath)) {
            for (Path path : paths) {
                if (pattern == null) {
                    pathCollection.addFirst(path);
                } else {
                    if (pattern.matcher(path.getFileName().toString()).matches()) {
                        pathCollection.addFirst(path);
                    }
                }

                if (recursive && Files.isDirectory(path)) {
                    getFiles(path, pattern, true, pathCollection);
                }
            }
        }
    }

    /**
     * 递归删除指定目录下的所有文件夹和文件
     *
     * @param rootPath 需要删除文件的目录
     * @throws IOException 文件删除异常类
     */
    public static void clearDir(String rootPath) throws IOException {
        clearDir(Paths.get(rootPath), "*");
    }

    /**
     * 递归删除指定目录下的所有文件夹和文件
     *
     * @param rootPath 需要删除文件的目录
     * @param glob     文件过滤表达式
     * @throws IOException 文件删除异常类
     */
    public static void clearDir(String rootPath, String glob) throws IOException {
        clearDir(Paths.get(rootPath), glob);
    }

    /**
     * 递归删除指定目录下的所有文件夹和文件
     *
     * @param rootPath 需要删除文件的目录
     * @throws IOException 文件删除异常类
     */
    public static void clearDir(Path rootPath) throws IOException {
        clearDir(rootPath, "*");
    }

    /**
     * 递归删除指定目录下的所有文件夹和文件
     *
     * @param rootPath 需要删除文件的目录
     * @param glob     文件过滤表达式
     * @throws IOException 文件删除异常类
     */
    public static void clearDir(Path rootPath, String glob) throws IOException {
        Collection<Path> pathCollection = getFiles(rootPath, glob, true);

        //删除所有的文件
        for (Path path : pathCollection) {
            if (!path.toFile().isDirectory()) {
                Files.delete(path);
            }
        }

        //删除所有的目录，需要按照层级来删除,目录越深越需要提前删除
        pathCollection = pathCollection.stream()
                .filter(p -> p.toFile().isDirectory())
                .sorted((p1, p2) -> Integer.compare(p2.toString().length(), p1.toString().length()))
                .collect(Collectors.toList());
        for (Path path : pathCollection) {
            path.toFile().delete();
        }
    }

    private static String appPath;

    /**
     * 获取应用程序的当前目录
     *
     * @return 应用程序目录
     */
    public static String getAppPath() {
        if (StringUtils.isBlank(appPath)) {
            //尝试获取容器的根目录
            appPath = System.getProperty("catalina.base");

            if (StringUtils.isBlank(appPath)) {
                //尝试获取default.properties所在的目录
                try {
                    appPath = FileConfigReader.getConfigFileBasePath();
                } catch (FileNotFoundException e) {
                    //TODO
                }
            }

            if (StringUtils.isBlank(appPath)) {
                appPath = Paths.get("").toAbsolutePath().toString();
            }

            System.out.println("appPath: " + appPath);
        }
        return appPath;
    }

    /**
     * 获取应用程序的当前目录
     *
     * @return 应用程序目录
     */
    public static String getResourceRootPath() {
        switch (SystemUtils.currentOperatorSystem()) {
            case WINDOWS:
                return PathUtils.class.getResource("/").getPath().substring(1).replace('/', File.separatorChar);
            default:
                return PathUtils.class.getResource("/").getPath();
        }
    }

    /**
     * 将URL转换为Path对象
     *
     * @param url 需要转换的URL对象
     * @return Path对象
     */
    public static Path fromUrl(URL url) {
        if (url == null) {
            return null;
        }
        String urlString = url.getPath();
        switch (SystemUtils.currentOperatorSystem()) {
            case WINDOWS:
                return Paths.get(url.getPath().substring(1).replace('/', File.separatorChar));
            default:
                return Paths.get(urlString);
        }
    }

    /**
     * 获取系统临时目录
     *
     * @return 系统临时路径
     */
    public static String getSysTempDir() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * 获取用户的工作目录
     *
     * @return 用户的工作目录
     */
    public static String getUserDir() {
        return System.getProperty("user.dir");
    }

    /**
     * 获取两个路径之间的相对路径
     *
     * @param rootPath 根路径
     * @param subPath  子路径
     * @return 它们之间的相对路径
     */
    public static String getRelativePath(String rootPath, String subPath) throws IOException {
        Validate.notBlank(rootPath, "根路径不能为空!");
        Validate.notBlank(subPath, "子路径不能为空!");

        boolean isSubPath;          //是否为子路径
        if (SystemUtils.currentOperatorSystem().equals(SystemUtils.OperatorSystemEnum.WINDOWS)) {
            //如果是windows操作系统,那么可以忽略大小写
            isSubPath = subPath.toLowerCase().startsWith(rootPath.toLowerCase());
        } else {
            isSubPath = subPath.startsWith(rootPath);
        }

        if (isSubPath) {
            String relativePath = subPath.substring(rootPath.length());
            return StringUtils.trim(relativePath, '/', '\\');
        } else {
            throw new IOException("两个路径不是包含关系");
        }
    }

    /**
     * 获取文件的扩展名
     *
     * @param fileName 文件名称
     * @return 文件扩展名，不包括点
     */
    public static String getFileExtensionName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return StringUtils.EMPTY;
        }

        //判断最后一个路径分隔符的位置，
        int splitIndex = fileName.lastIndexOf('/');
        if (splitIndex < 0) {
            splitIndex = fileName.lastIndexOf('\\');
        }

        //如果文件路径分隔符在点之后，表示文件没有扩展名
        if (dotIndex < splitIndex) {
            return StringUtils.EMPTY;
        } else {        //否则取出文件的扩展名，不含点号本身
            return fileName.substring(dotIndex + 1);
        }
    }

    /**
     * 获取文件内容的MD5编码的值
     *
     * @param file 文件类
     * @return 文件内容MD5编码的值
     */
    public static String getFileMd5Code(File file) throws IOException {
        String value = null;
        try (FileInputStream in = new FileInputStream(file)) {
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * 创建文件存储的目录，因为在保存文件时，如果相关的文件夹不存在，
     * 那么就会抛出异常，所以我们应该先创建出储存改文件的目录
     *
     * @param file 文件对象
     */
    public static void createFileDir(File file) throws IOException {
        Validate.notNull(file);

        if (!file.exists()) {           //文件不存在，那么创建目录
            Path path = Paths.get(file.getAbsolutePath());
            if (file.isDirectory()) {
                Files.createDirectories(path);
            } else {
                path = path.getParent();
                if (!Files.exists(path)) {
                    Files.createDirectories(path);
                }
            }
        }
    }

    /**
     * 创建文件存储的目录，如果目录已经存在，那么将不执行任何操作
     *
     * @param path 目录完整路径
     */
    public static boolean createDir(String path) {
        Validate.notBlank(path);
        File file = new File(path);
        if (!file.exists()) {           //文件不存在，那么创建目录
            return file.mkdirs();
        } else {
            return true;
        }
    }
}
