package com.dongzy.common.config;

import com.dongzy.common.common.Validate;
import com.dongzy.common.common.io.ClassLoaderWrapper;
import com.dongzy.common.common.io.PathUtils;
import com.dongzy.common.common.text.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于文件的自定义配置实现类
 */
public class FileConfigReader implements IConfigReader {

    //配置文件的默认扩展名
    private final static String FILE_EXT = ".properties";
    private final static Map<String, SafeProperties> PROPERTIES_MAP = new ConcurrentHashMap<>();
    //保存核心配置文件的访问目录（不含文件本身）
    private static String configFileBasePath;

    /**
     * 获取环境变量中配置的基础路径
     *
     * @return 环境变量
     */
    public static String getConfigFileBasePathByEnvironmentVariable() {
        String basePath = null;
        if (StringUtils.notBlank(System.getProperty(SystemConfig.ZY_CONFIG_FILE_BASE_PATH))) {
            File file = new File(PathUtils.joinPath(System.getProperty(SystemConfig.ZY_CONFIG_FILE_BASE_PATH), "/"));
            if (file.exists()) {
                basePath = file.getAbsolutePath();
            } else {
                throw new IllegalArgumentException(SystemConfig.ZY_CONFIG_FILE_BASE_PATH + " is not found!");
            }
        }
        return basePath;
    }

    /**
     * 获取核心配置文件的目录，其他配置文件可能会参考此路径
     */
    public static String getConfigFileBasePath() throws FileNotFoundException {
        if (configFileBasePath == null) {
            String basePath = getConfigFileBasePathByEnvironmentVariable();
            if (StringUtils.isBlank(basePath)) {
                File file = new ClassLoaderWrapper().getResourceFile(getFileName(SystemConfig.DEFAULT_NAMESPACE));
                if (file.exists()) {
                    configFileBasePath = file.getParent();
                }
            }
        }
        return configFileBasePath;
    }

    /**
     * 获取某个配置文件单位完整路径
     *
     * @param namespace 需要查找的命名空间
     * @return 完整路径
     */
    public static File getConfigFile(String namespace) throws FileNotFoundException {
        return new ClassLoaderWrapper().getResourceFile(getFileName(namespace));
    }

    //注意，在文件类型配置方案中，因为不支持跨应用获取配置，所以appCode不起任何作用
    @Override
    public Collection<ConfigItemInfo> getConfigItems(String namespace, String appCode) {
        Properties properties = getProperties(namespace);
        Collection<ConfigItemInfo> itemInfoes = new ArrayList<>();
        for (Object o : properties.keySet()) {
            String key = (String) o;
            ConfigItemInfo configItemInfo = new ConfigItemInfo(appCode, namespace, key);
            configItemInfo.setValue(properties.getProperty(key));
            configItemInfo.setEnabled(true);
            itemInfoes.add(configItemInfo);
        }
        return itemInfoes;
    }

    /**
     * 获取指定namespace的自定义配置读取类
     *
     * @param namespace 命名空间
     * @return 配置读取类
     */
    public CustomConfig getCustomConfig(String namespace) {
        return new CustomConfig(this, "default", namespace);
    }

    /**
     * 将指定的配置保存到文件中，（没有修改的值会保持不变，如果需要清空配置，将value设置为空串）
     *
     * @param namespace 需要保存的命名空间
     * @param map       需要保留的值
     */
    public void saveToFile(String namespace, Map<String, String> map) throws IOException {
        Validate.notBlank(namespace, "namespace不能为空");

        File file = new ClassLoaderWrapper().getResourceFile(getFileName(namespace));
        if (file.exists()) {
            try (FileOutputStream outputFile = new FileOutputStream(file)) {
                SafeProperties properties = getProperties(namespace);
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    properties.setProperty(entry.getKey(), entry.getValue());
                }
                properties.store(outputFile, "customconfig");
            }
        }
    }

    private SafeProperties getProperties(String namespace) {
        Validate.notBlank(namespace, "namespace不能为空");
        namespace = namespace.trim();
        SafeProperties properties = PROPERTIES_MAP.getOrDefault(namespace, null);
        if (properties == null) {
            properties = new SafeProperties();
            try (InputStream in = new ClassLoaderWrapper().getResourceStream(getFileName(namespace))) {
                properties.load(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
            PROPERTIES_MAP.put(namespace, properties);
        }
        return properties;
    }

    //根据namespace获取配置文件名
    private static String getFileName(String namespace) {
        return namespace + FILE_EXT;
    }
}
