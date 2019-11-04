package com.dongzy.common.config;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义配置管理类
 */
public final class CustomConfigManager {

    //缓存配置读取类，key为appCode + namespace
    private static final Map<String, CustomConfig> CUSTOM_CONFIG_MAP = new ConcurrentHashMap<>();
    //自定义配置查询实现类
    private static IConfigReader configReader;

    /**
     * 获取自定配置的实现类
     */
    private static IConfigReader getConfigReader() {
        if (configReader == null) {
            switch (SystemConfig.getConfigType()) {
                case FILE:
                    configReader = new FileConfigReader();
                    break;
                case NACOS:
                    configReader = new NacosConfigReader();
                    break;
            }
        }
        return configReader;
    }

    /**
     * 设定配置获取类
     *
     * @param configReader 配置获取类
     */
    public static void setConfigReader(IConfigReader configReader) {
        CustomConfigManager.configReader = configReader;
    }

    /**
     * 清除所有缓存数据
     */
    public synchronized static void clearCache() {
        CUSTOM_CONFIG_MAP.clear();
    }

    /**
     * 清除单个应用下面的所有缓存数据
     *
     * @param appCode app编码
     */
    public synchronized static void clearCache(String appCode) {
        Iterator<String> iterator = CUSTOM_CONFIG_MAP.keySet().iterator();
        String pre = appCode + "=";
        while (iterator.hasNext()) {
            if (iterator.next().startsWith(pre)) {
                iterator.remove();
            }
        }
    }

    /**
     * 清除某个命名空间下面的所有缓存数据
     *
     * @param appCode   app编码
     * @param namespace 命名空间
     */
    public synchronized static void clearCache(String appCode, String namespace) {
        CUSTOM_CONFIG_MAP.remove(String.format("%s=%s", appCode, namespace));
    }

    /**
     * 获取配置访问类
     *
     * @return 配置访问类
     */
    public static CustomConfig getCustomConfig() {
        return getCustomConfig(SystemConfig.DEFAULT_NAMESPACE);
    }

    /**
     * 获取配置访问类
     *
     * @return 配置访问类
     */
    public static CustomConfig getCustomConfig(String namespace) {
        return getCustomConfig(SystemConfig.getAppCode(), namespace);
    }

    /**
     * 获取配置访问类
     *
     * @return 配置访问类
     */
    public static CustomConfig getCustomConfig(String appCode, String namespace) {
        String key = String.format("%s=%s", appCode, namespace);
        CustomConfig customConfig = CUSTOM_CONFIG_MAP.get(key);
        if (customConfig == null) {
            customConfig = new CustomConfig(getConfigReader(), appCode, namespace);
            CUSTOM_CONFIG_MAP.put(key, customConfig);
        }
        return customConfig;
    }
}
