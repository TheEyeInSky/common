package com.dongzy.common.common.io;

import com.dongzy.common.common.Validate;
import com.dongzy.common.common.text.StringUtils;
import com.dongzy.common.config.FileConfigReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 自定义的类加载器，主要实现了以下的功能：
 * 1、比系统默认的加载器扩展了查找范围，不但能够jar包范围内搜索，还能从其他jar文件中查找实现内查找
 * 2、能够返回找到到文件、URL或流对象
 * 3、从代码包中加载实现类的功能
 *
 * @author zouyong
 * @since JDK1.5
 */
public class ClassLoaderWrapper {

    //匹配带盘符的绝对路径
    private final static Pattern WIN_PATTERN = Pattern.compile("^[a-zA-Z]:[\\\\/].*?$");
    private static List<ClassLoader> customClassLoaders = new ArrayList<>();
    private ClassLoader systemClassLoader;      //typically the class loader used to start the application

    /**
     * 添加默认的类加载器
     *
     * @param classLoader 类加载器
     */
    public synchronized static void addClassLoader(ClassLoader classLoader) {
        customClassLoaders.add(classLoader);
    }

    /**
     * 默认构造器
     */
    public ClassLoaderWrapper() {
        systemClassLoader = ClassLoader.getSystemClassLoader();
    }

    /**
     * 从当前位置获取资源地址
     *
     * @param resource 资源名称
     * @return 资源地址
     * @throws FileNotFoundException 未找到文件
     */
    public File getResourceFile(String resource) throws FileNotFoundException {
        return getResourceFile(resource, null);
    }

    /**
     * 使用指定的加载器获取资源
     *
     * @param resource    资源名称
     * @param classLoader 加载器
     * @return 资源地址
     * @throws FileNotFoundException 未找到文件
     */
    public File getResourceFile(String resource, ClassLoader classLoader) throws FileNotFoundException {
        return new File(getResource(resource, classLoader).getPath());
    }

    /**
     * 从当前位置获取资源地址
     *
     * @param resource 资源名称
     * @return 资源地址
     * @throws FileNotFoundException 未找到文件
     */
    public URL getResource(String resource) throws FileNotFoundException {
        return getResource(resource, null);
    }

    /**
     * 使用指定的加载器获取资源
     *
     * @param resource    资源名称
     * @param classLoader 加载器
     * @return 资源地址
     * @throws FileNotFoundException 未找到文件
     */
    public URL getResource(String resource, ClassLoader classLoader) throws FileNotFoundException {
        List<URL> urls = getResources(resource, getClassLoaders(classLoader), false);
        if (urls.size() == 1) {
            return urls.get(0);
        } else {
            throw new FileNotFoundException("没有找到资源：" + resource);
        }
    }

    /**
     * 从当前位置获取资源地址
     *
     * @param resource 资源名称
     * @return 资源地址
     * @throws FileNotFoundException 未找到文件
     */
    public List<URL> getResources(String resource) throws FileNotFoundException {
        return getResources(resource, null);
    }

    /**
     * 使用指定的加载器获取资源
     *
     * @param resource    资源名称
     * @param classLoader 加载器
     * @return 资源地址
     * @throws FileNotFoundException 未找到文件
     */
    public List<URL> getResources(String resource, ClassLoader classLoader) throws FileNotFoundException {
        return getResources(resource, getClassLoaders(classLoader), true);
    }

    //获取资源，如果需要查找所有可能，那么需要将getAll设置为true
    private List<URL> getResources(String resource, ClassLoader[] classLoaders, boolean getAll) throws FileNotFoundException {

        Validate.notNull(resource, "资源名称为不能为空！");
        Validate.notNull(classLoaders, "classLoaders不能为空！");
        List<URL> urls = new ArrayList<>();
        try {
            //如果路径是绝对路径，那么
            if (WIN_PATTERN.matcher(resource).matches()) {
                urls.add(new File(resource).toURI().toURL());
                return urls;
            }
            //如果采用classpath:开头，那么去掉他
            if (resource.startsWith("classpath:")) {
                resource = resource.substring("classpath:".length());
            }
            URL url;
            for (ClassLoader classLoader : classLoaders) {
                if (classLoader != null) {
                    url = classLoader.getResource(resource);
                    if (url == null) {
                        url = classLoader.getResource("/" + resource);
                    }
                    if (url != null) {
                        urls.add(url);
                        if (!getAll) {
                            return urls;
                        }
                    }
                }
            }

            //如果没有找到配置文件，那么尝试去外部指定的路径中获取
            if (urls.size() == 0) {
                String basePath = FileConfigReader.getConfigFileBasePathByEnvironmentVariable();
                if (StringUtils.notBlank(basePath)) {
                    File file = new File(PathUtils.joinPath(basePath, resource));
                    if (file.exists()) {
                        urls.add(file.toURI().toURL());
                    }
                }
            }
            return urls;
        } catch (MalformedURLException e) {
            throw new FileNotFoundException(e.getMessage());
        }
    }

    /**
     * get a resource as a input stream using the current class path
     *
     * @param resource 资源名称
     * @return 流对象
     * @throws FileNotFoundException 未找到文件
     */
    public InputStream getResourceStream(String resource) throws FileNotFoundException {
        return getResourceStream(resource, null);
    }

    /**
     * 使用指定的类加载器获取资源的流对象
     *
     * @param resource    资源名称
     * @param classLoader 类加载器
     * @return 流对象
     * @throws FileNotFoundException 未找到文件
     */
    public InputStream getResourceStream(String resource, ClassLoader classLoader) throws FileNotFoundException {
        return new FileInputStream(getResourceFile(resource, classLoader));
    }

    /**
     * find a class on the classpath
     *
     * @param name 类名称
     * @return 类对象
     * @throws ClassNotFoundException 未找到类异常
     */
    public Class<?> classForName(String name) throws ClassNotFoundException {
        return classForName(name, getClassLoaders(null));
    }

    /**
     * 使用指定的类加载器加载对象
     *
     * @param name        类名称
     * @param classLoader 类加载器
     * @return 类对象
     * @throws ClassNotFoundException 未找到类异常
     */
    public Class<?> classForName(String name, ClassLoader classLoader) throws ClassNotFoundException {
        return classForName(name, getClassLoaders(classLoader));
    }

    private Class<?> classForName(String name, ClassLoader... classLoaders) throws ClassNotFoundException {
        for (ClassLoader classLoader : classLoaders) {
            if (classLoader != null) {
                Class<?> c = Class.forName(name, true, classLoader);
                if (c != null) return c;
            }
        }

        throw new ClassNotFoundException("Cannot find class: " + name);
    }

    /**
     * 获取类记载器集合
     *
     * @param classLoader 需要添加的类加载器
     * @return 类加载器集合
     */
    private ClassLoader[] getClassLoaders(ClassLoader classLoader) {
        int defaultLoaderSize = 4;
        ClassLoader[] classLoaders = new ClassLoader[customClassLoaders.size() + defaultLoaderSize];
        classLoaders[0] = classLoader;
        classLoaders[1] = Thread.currentThread().getContextClassLoader();
        classLoaders[2] = getClass().getClassLoader();
        classLoaders[3] = systemClassLoader;

        for (int i = defaultLoaderSize; i < classLoaders.length; i++) {
            classLoaders[i] = customClassLoaders.get(i - defaultLoaderSize);
        }

        return classLoaders;
    }
}
