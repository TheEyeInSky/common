package com.dongzy.common.cache.service.impl;

import java.util.Date;

/**
 * @author dongzy
 * @Desc
 * @date 2019/9/16.
 */
public final class CacheItem<T> {
    private String key;
    private T value;
    private Long absExpiry; // 绝对过期时间

    /**
     * 根据传入的参数构造函数
     *
     * @param key   缓存项的key
     * @param value 缓存项的值
     * @param date  绝对过期时间
     */
    public CacheItem(String key, T value, Date date) {
        this.key = key;
        this.value = value;
        this.absExpiry = (date == null) ? null : date.getTime();
    }

    /**
     * 获取缓存项的Key
     *
     * @return 缓存项的Key
     */
    public String getKey() {
        return key;
    }

    /**
     * 获取缓存项的值
     *
     * @return 缓存项的值
     */
    public T getValue() {
        return value;
    }

    /**
     * 获取绝对过期的时间
     *
     * @return 过期的时间
     */
    public Long getAbsExpiry() {
        return absExpiry;
    }
}
