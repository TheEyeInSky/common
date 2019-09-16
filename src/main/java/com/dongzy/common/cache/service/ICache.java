package com.dongzy.common.cache.service;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * @author dongzy
 * @Desc
 * @date 2019/9/16.
 */

public interface ICache<T> {
    /**
     * 更新或新增缓存的对象，如果key已经存在，那么更新现有的对象，如果不存在，那么新增一个缓存对象
     * 如果key为null或value为null，将会抛出IllegalArgumentException异常
     *
     * @param key   缓存的KEY
     * @param value 缓存的值
     */
    void set(String key, T value);

    /**
     * 更新或新增缓存的对象，如果key已经存在，那么更新现有的对象，如果不存在，那么新增一个缓存对象
     * 如果key为null或value为null，将会抛出IllegalArgumentException异常
     *
     * @param key   缓存的KEY
     * @param value 缓存的值
     * @param date  缓存的绝对过期缓存周期，超过后将会被清除
     */
    void set(String key, T value, Date date);

    /**
     * 更新或新增缓存的对象，如果key已经存在，那么更新现有的对象，如果不存在，那么新增一个缓存对象
     * 如果key为null或value为null，将会抛出IllegalArgumentException异常
     *
     * @param map 缓存的KEY
     */
    void setMap(Map<String, T> map);

    /**
     * 更新或新增缓存的对象，如果key已经存在，那么更新现有的对象，如果不存在，那么新增一个缓存对象
     * 如果key为null或value为null，将会抛出IllegalArgumentException异常
     *
     * @param map  缓存的KEY
     * @param date 缓存的绝对过期时间
     */
    void setMap(Map<String, T> map, Date date);

    /**
     * 获取缓存的对象，如何key为null，那么返回的值永远也是null
     *
     * @param key 缓存KEY
     * @return 缓存的值
     */
    T get(String key);

    /**
     * 获取指定key集合对应的key和value对的hashMap对象
     *
     * @param keys key集合
     * @return 包含指定key集合的HashMap对象
     */
    Map<String, T> getMap(Collection<String> keys);

    /**
     * 移除指定key对应的缓存，如何key为null，或者空串，将不会更改任何缓存数据
     *
     * @param key 缓存的key
     */
    void remove(String key);

    /**
     * 移除指定key对应的缓存，如何key为null，或者空串，将不会更改任何缓存数据
     *
     * @param keys 缓存的key的集合
     */
    void remove(Collection<String> keys);

    /**
     * 清除当前缓存类的所有缓存数据
     */
    void clear();

    /**
     * 判断该key是否在缓存集合中
     *
     * @param key 缓存的key
     * @return 是否包含在缓存集合中
     */
    boolean containsKey(String key);
}
