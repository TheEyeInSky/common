package com.dongzy.common.common.reflect;

import org.slf4j.Logger;
import com.dongzy.common.common.Validate;
import com.dongzy.common.common.io.ClassLoaderWrapper;
import com.dongzy.common.common.io.PathUtils;
import com.dongzy.common.log.TextLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 用户检索类的方法
 */
public class ClassFind {

    private static final Logger LOGGER = TextLoggerFactory.getInstance().getLogger(ClassFind.class);

    /**
     * jar中的文件路径分隔符
     */
    private static final char SLASH_CHAR = '/';
    /**
     * 包名分隔符
     */
    private static final char DOT_CHAR = '.';
    /**
     * 定义过滤文件的后缀
     */
    private static final String FILE_GLOB = "*.class";

    /**
     * 在当前项目中寻找指定包下的所有类
     * 搜索范围包括本项目中编译的class和jar包中的class
     *
     * @param packageName 用'.'分隔的包名
     * @param recursive   是否递归搜索
     * @return 该包名下的所有类
     */
    public static Set<Class<?>> getClass(String packageName, boolean recursive) {
        Set<Class<?>> classList = new HashSet<>();
        classList.addAll(getClassInFile(packageName, recursive));
        classList.addAll(getClassInJar(packageName, recursive));
        return classList;
    }

    /**
     * 在当前项目中寻找指定包下的所有类，按照文件目录查找
     * 适用于当前项目中的编译的class，不查找引入jar中的class
     *
     * @param packageName 用'.'分隔的包名
     * @param recursive   是否递归搜索
     * @return 该包名下的所有类
     */
    public static Set<Class<?>> getClassInFile(String packageName, boolean recursive) {
        Validate.notBlank(packageName);

        Set<Class<?>> classList = new HashSet<>();

        try {
            Collection<URL> urls = new ClassLoaderWrapper().getResources(packageName.replace(DOT_CHAR, SLASH_CHAR));
            for (URL url : urls) {
                if (!"file".equals(url.getProtocol())) {
                    continue;
                }

                String rootPath = new File(url.getPath()).toString();
                Collection<Path> paths = PathUtils.getFiles(Paths.get(rootPath), FILE_GLOB, recursive);
                for (Path path : paths) {
                    String pathStr = path.toString().replace(File.separatorChar, DOT_CHAR);
                    int beginIndex = pathStr.lastIndexOf(packageName);
                    //去掉根路径
                    pathStr = pathStr.substring(beginIndex);
                    //去掉.class扩展名
                    pathStr = pathStr.substring(0, pathStr.length() - FILE_GLOB.length() + 1);
                    String className = pathStr.replace(File.separatorChar, DOT_CHAR);
                    try {
                        classList.add(Class.forName(className));
                    } catch (Throwable e) {
                        LOGGER.warn("创建类对象" + className + "时发生异常！", e);
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.warn("扫描包时发生未知的异常！", ex);
        }

        return classList;
    }

    /**
     * 在给定的jar包中寻找指定包下的所有类
     * 适用于查找被引入jar中的类
     *
     * @param packageName 用'.'分隔的包名
     * @param recursive   是否递归搜索
     * @return 该包名下的所有类
     */
    private static Set<Class<?>> getClassInJar(String packageName, boolean recursive) {

        Set<Class<?>> classList = new HashSet<>();

        try {
            Collection<URL> urls = new ClassLoaderWrapper().getResources(packageName.replace(DOT_CHAR, SLASH_CHAR));
            for (URL url : urls) {
                if (!"jar".equals(url.getProtocol())) {
                    continue;
                }

                JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
                //该迭代器会递归得到该jar底下所有的目录和文件
                Enumeration<JarEntry> iterator = jar.entries();
                while (iterator.hasMoreElements()) {
                    //这里拿到的一般的"aa/bb/.../cc.class"格式的Entry或 "包路径"
                    JarEntry jarEntry = iterator.nextElement();
                    if (jarEntry.isDirectory()) {
                        continue;
                    }

                    String name = jarEntry.getName();
                    //对于拿到的文件,要去除末尾的.class
                    if (name.endsWith(".class")) {
                        int lastSlashIndex = name.lastIndexOf(SLASH_CHAR);
                        name = name.replace(SLASH_CHAR, DOT_CHAR);
                        if (name.startsWith(packageName)) {
                            if (recursive || packageName.length() == lastSlashIndex) {
                                name = name.substring(0, name.length() - 6);
                                try {
                                    classList.add(Class.forName(name));
                                } catch (Throwable e) {
                                    LOGGER.warn("创建类对象" + name + "时发生异常！", e);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.warn("扫描包时发生未知的异常！", ex);
        }

        return classList;
    }

    /**
     * 获取指定类的所有的子类
     *
     * @param packageName 需要查找的命名空间
     * @param parentClass 父类
     * @return 所有的子类
     */
    public static Set<Class<?>> getSubClass(String packageName, Class<?> parentClass) {
        Validate.notBlank(packageName);
        Validate.notNull(parentClass);

        Set<Class<?>> classList = new HashSet<>();
        for (Class<?> cl : getClass(packageName, true)) {
            if (parentClass.isAssignableFrom(cl)) {
                classList.add(cl);
            }
        }
        return classList;
    }

    /**
     * 获取指定注解的类
     *
     * @param packageName 需要查找的命名空间
     * @param annotation  注解类
     * @return 所有的子类
     */
    public static Set<Class<?>> getAnnotationClass(String packageName, Class<? extends Annotation> annotation) {
        Validate.notBlank(packageName);
        Validate.notNull(annotation);

        Set<Class<?>> classList = new HashSet<>();
        for (Class<?> cl : getClass(packageName, true)) {
            if (cl.getDeclaredAnnotation(annotation) != null) {
                classList.add(cl);
            }
        }
        return classList;
    }
}
