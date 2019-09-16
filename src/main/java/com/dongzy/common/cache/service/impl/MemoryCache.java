package com.dongzy.common.cache.service.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author dongzy
 * @Desc
 * @date 2019/9/16.
 */

public final class MemoryCache<T> extends Cache<T> {
    private final Map<String, CacheItem<T>> cacheMap; // 缓存对象map
    private final static MemoryCache memoryCache = new MemoryCache();
    private final ScheduledExecutorService scheduledExecutorService;

    /**
     * 定义内存回收的类
     */
    private class ClearData extends Thread {

        public ClearData() {
            this.setDaemon(true);                           //将当前线程设置为守护线程
            this.setPriority(Thread.MIN_PRIORITY);          //降低线程的优先级
        }

        @Override
        public void run() {
            for (CacheItem<T> cacheItem : cacheMap.values()) {
                if (cacheItem.getAbsExpiry() != null && cacheItem.getAbsExpiry() < System.currentTimeMillis()) {
                    cacheMap.remove(cacheItem.getKey()); // 移除过期的缓存对象
                }
                Thread.yield();
            }
        }
    }

    /**
     * 传入一个自定义的缓存对象来保存数据
     *
     * @param cacheMap 自定义的缓存对象
     */
    public MemoryCache(Map<String, CacheItem<T>> cacheMap) {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        //设定每30分钟回收一次内存。
        scheduledExecutorService.scheduleWithFixedDelay(new MemoryCache.ClearData(), 30, 30, TimeUnit.MINUTES);
        this.cacheMap = cacheMap;
    }

    /**
     * 构造函数，创建一个定时清理缓存的机制
     */
    public MemoryCache() {
        this(new HashMap<>());
    }

    /**
     * 获取默认的缓存实例，本实例在每个站点是全部唯一的。
     *
     * @return 全局唯一缓存对象
     */
    public static MemoryCache getInstance() {
        return memoryCache;
    }

    @Override
    synchronized public void set(String key, T value) {
        set(key, value, null);
    }

    @Override
    synchronized public void set(String key, T value, Date date) {
        notNull(key, "缓存的key不能为空！");

        CacheItem<T> cacheItem = new CacheItem<>(key, value, date);
        cacheMap.put(key, cacheItem);
    }

    @Override
    public T get(String key) {
        notNull(key, "缓存的key不能为空！");

        CacheItem<T> cacheItem = cacheMap.getOrDefault(key, null);
        if (cacheItem != null) {
            // 如果缓存没有失效，那么返回缓存
            if (isExpiry(cacheItem)) {
                cacheMap.remove(key); // 移除过期的缓存对象
            } else {
                return cacheItem.getValue();
            }
        }
        return null;
    }

    @Override
    synchronized public void remove(String key) {
        notNull(key, "缓存的key不能为空！");
        cacheMap.remove(key);
    }

    @Override
    synchronized public void remove(Collection<String> keys) {
        for (String key : keys) {
            remove(key);
        }
    }

    @Override
    synchronized public void clear() {
        cacheMap.clear();
    }

    @Override
    public boolean containsKey(String key) {
        CacheItem<T> cacheItem = cacheMap.getOrDefault(key, null);
        if (cacheItem != null) {
            // 如果缓存没有失效，那么返回缓存
            if (isExpiry(cacheItem)) {
                cacheMap.remove(key); // 移除过期的缓存对象
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    //判断缓存是否已经过期
    private boolean isExpiry(CacheItem<T> cacheItem) {
        if (cacheItem.getAbsExpiry() == null) {
            return false;
        }
        return cacheItem.getAbsExpiry() < System.currentTimeMillis();
    }

    public static void notNull(final Object object, final String message, final Object... values) {
        if (object == null) {
            throw new NullPointerException(String.format(message, values));
        }
    }
}
