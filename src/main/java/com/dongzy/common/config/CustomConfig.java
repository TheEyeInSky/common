package com.dongzy.common.config;

import com.dongzy.common.common.BooleanUtils;
import com.dongzy.common.common.NumberUtils;
import com.dongzy.common.common.Validate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义配置查询类
 */
public class CustomConfig {

    //缓存配置值的对象
    private final Map<String, String> VALUE_CACHE = new ConcurrentHashMap<>();

    private String appCode;
    private String namespace;

    CustomConfig(IConfigReader configReader, String appCode, String namespace) {
        Validate.notNull(configReader, "configReader不能为空");
        Validate.notBlank(appCode, "appCode不能为空");
        Validate.notBlank(namespace, "namespace不能为空");

        this.appCode = appCode;
        this.namespace = namespace;

        Collection<ConfigItemInfo> configItems = configReader.getConfigItems(namespace, appCode);
        for (ConfigItemInfo configItem : configItems) {
            VALUE_CACHE.put(configItem.getKey(), configItem.getValue());
        }
    }

    public String getAppCode() {
        return appCode;
    }

    public String getNamespace() {
        return namespace;
    }

    /**
     * 获取指定配置项的值
     *
     * @param key 查询的key
     * @return 配置项的值
     */
    public String getValue(String key) {
        return getValue(key, null);
    }

    /**
     * 获取指定配置项的值
     *
     * @param key          查询的key
     * @param defaultValue 默认值
     * @return 配置项的值
     */
    public String getValue(String key, String defaultValue) {
        return VALUE_CACHE.getOrDefault(key, defaultValue);
    }

    /**
     * 获取某个节点的所有子节点
     *
     * @param key 需要查找的key
     */
    public Map<String, String> getSubConfig(String key) {
        Validate.notBlank(key, "key不能为空");

        Map<String, String> map = new HashMap<>();
        for (String k : VALUE_CACHE.keySet()) {
            if (k.startsWith(key) && !k.equals(key)) {
                map.put(k, VALUE_CACHE.get(k));
            }
        }
        return map;
    }

    /**
     * 获取布尔类型的值
     *
     * @param key          查询的key
     * @param defaultValue 默认值
     * @return 配置项的值
     */
    public boolean getBooleanValue(String key, boolean defaultValue) {
        return BooleanUtils.toBoolean(VALUE_CACHE.get(key), defaultValue);
    }

    /**
     * 获取数子类型的值
     *
     * @param key          查询的key
     * @param defaultValue 默认值
     * @return 配置项的值
     */
    public int getIntegerValue(String key, int defaultValue) {
        return NumberUtils.toInt(VALUE_CACHE.get(key), defaultValue);
    }

    /**
     * 获取所有配置值
     *
     * @return 所有配置值
     */
    public Map<String, String> getAllValues() {
        return VALUE_CACHE;
    }
}
