package com.dongzy.common.common.caching;

/**
 * 内存缓存的对象
 *
 * @author zouyong
 * @since JDK1.5
 */
public interface IMethodResult<T> {

    /**
     * 获取缓存项的值
     *
     * @param key 缓存的key
     * @return 缓存的值
     */
    T get(String key);

    /**
     * 判断该key是否在缓存集合中
     *
     * @param key 缓存的key
     * @return 是否包含在缓存集合中
     */
    boolean containsKey(String key);
}
