package com.dongzy.common.cache.service.impl;

import com.dongzy.common.cache.service.ICache;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dongzy
 * @Desc
 * @date 2019/9/16.
 */
public abstract class Cache<T> implements ICache<T> {
    @Override
    synchronized public void setMap(Map<String, T> map) {
        setMap(map, null);
    }

    @Override
    synchronized public void setMap(Map<String, T> map, Date date) {
        for (String key : map.keySet()) {
            if (key != null) {
                set(key, map.get(key), date);
            }
        }
    }

    @Override
    public Map<String, T> getMap(Collection<String> keys) {
        notNull(keys, "获取缓存的keys不能为空！");

        Map<String, T> newMap = new HashMap<>();
        for (String key : keys) {
            if (key != null) {
                newMap.put(key, get(key));
            }
        }
        return newMap;
    }

    public static void notNull(final Object object, final String message, final Object... values) {
        if (object == null) {
            throw new NullPointerException(String.format(message, values));
        }
    }
}
