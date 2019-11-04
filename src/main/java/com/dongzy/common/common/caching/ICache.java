package com.dongzy.common.common.caching;


import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 系统缓存的接口类，本接口定义了缓存的基本功能
 * 主要功能点如下：
 * 1、设置是否启用一级缓存，以及一级缓存的存活时间（默认不启用一级缓存功能）
 * 对于对性能要求极高，而对于数据新鲜度不是那么敏感的缓存数据，可以考虑启用一级缓存
 * 比如数据库数据、redis数据、memcached数据、mongodb数据等，
 * 一级缓存会将每次查询的结果在系统内存中进行保存，超过一级缓存保存周期的数据会被清理掉，下次访问时会重建一级缓存，如此循环往复。
 * 因为一级缓存会被定期的清理掉，所以能够一定程度确保系统内存不被占用太多。
 * <p>
 * 例如：我们对角色表的全表数据进行了缓存，并设置缓存周期为30秒，那么第一次访问的时候，因为没有一级缓存，系统会从数据库中查询出数据，并自动
 * 存入一级缓存中，如果下次访问的时间间隔没有超过30秒，那么将知直接使用以及缓存中的数据，不会重新查询数据库。如果某次查询超过了30秒，那么
 * 系统会重新查询数据库，并将查询出的结果放入一级缓存。
 * <p>
 * 2、获取缓存信息，如果响应的key不存在，将返回null
 * 3、清空缓存信息，会清空当前缓存类中的所有缓存数据。
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
