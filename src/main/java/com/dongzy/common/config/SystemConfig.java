package com.dongzy.common.config;

import java.io.IOException;
import java.util.Map;

/**
 * 系统配置类文件，用户读取和保存系统的配置信息，这些配置都是用户自定义的配置信息
 *
 * @author zouyong
 * @since SDK1.8
 */
public class SystemConfig {

    /**
     * app编码配置key
     */
    public static final String APP_CODE = "appCode";
    /**
     * 配置存储类型配置key
     */
    public static final String CONFIG_TYPE = "configType";
    /**
     * 默认的命名空间配置key
     */
    public final static String DEFAULT_NAMESPACE = "default";
    /**
     * 进入自定义配置管理的密码
     */
    public final static String CONFIG_MANAGE_PASSWORD = "configManagePassword";
    /**
     * 传入的配置文件根目录位置
     */
    public final static String ZY_CONFIG_FILE_BASE_PATH = "zyConfigFileBasePath";
    /**
     * Nacos服务器地址
     */
    public final static String NACOS_SERVER_HOST = "nacosServerHost";
    /**
     * Nacos服务器地址
     */
    public final static String NACOS_SERVER_PORT = "nacosServerPort";
    /**
     * Nacos服务注册的默认版本
     */
    public final static String NACOS_SERVER_VERSION = "nacosServerVersion";

    //默认的文件读取配置实现类
    private static final CustomConfig CORE_CUSTOM_CONFIG = new FileConfigReader().getCustomConfig(DEFAULT_NAMESPACE);
    //站点code
    private static String appCode;
    //配置存储类型
    private static ConfigSourceEnum configSourceEnum;

    /**
     * 获取app的code值
     *
     * @return appCode
     */
    public static String getAppCode() {
        if (appCode == null) {
            appCode = getConfig().getValue(APP_CODE, null);
            if (appCode == null) {
                throw new IllegalStateException("无法找到appCode配置");
            }
        }
        return appCode;
    }

    /**
     * 获取配置保存类型，默认为File类型
     */
    public static ConfigSourceEnum getConfigType() {
        if (configSourceEnum == null) {
            configSourceEnum = ConfigSourceEnum.valueOf(getConfig().getValue(CONFIG_TYPE, "FILE"));
        }
        return configSourceEnum;
    }

    /**
     * 获取核心系统配置读取类
     */
    public static CustomConfig getConfig() {
        return CORE_CUSTOM_CONFIG;
    }

    /**
     * 保存系统的配置信息
     */
    public static void saveConfig(Map<String, String> map) throws IOException {
        new FileConfigReader().saveToFile(DEFAULT_NAMESPACE, map);
    }
}
