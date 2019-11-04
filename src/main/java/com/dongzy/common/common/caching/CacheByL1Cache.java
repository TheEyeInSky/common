package com.dongzy.common.common.caching;

import com.dongzy.common.common.Validate;

import java.lang.ref.SoftReference;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 实现缓存基础接口的抽象类，主要包含以下的功能
 * 1、一级缓存的统一实现
 * 2、获取数据的统一底层接口的实现
 * 3、一些缓存需要的公共基础方法
 * <p>
 * 注意：
 * 1、一级缓存的默认有效期为60秒，用户可以通过setL1CacheTimeout方法更改。
 * 2、默认不启用一级缓存，用户可以通过setEnableL1Cache方法来启用一级缓存
 *
 * @author zouyong
 * @since JDK1.6
 */
public abstract class CacheByL1Cache<T> extends Cache<T> {

    private final Lock lock = new ReentrantLock();
    protected final MemoryCache<SoftReference<T>> memoryL1Cache = new MemoryCache();        //一级缓存
    protected long l1Timeout = 60 * 1000;          //一级缓存默认缓存时间为60秒
    protected boolean enableL1Cache;

    /**
     * 从缓存源实时获取数据
     *
     * @param key 缓存的key
     * @return 缓存的值
     */
    protected abstract T getReal(String key);

    @Override
    public T get(String key) {
        Validate.notNull(key, "key不能为空！");
        T value;
        if (enableL1Cache) {       //如果启用了一级缓存，那么
            boolean isContains = memoryL1Cache.containsKey(key);
            if (!isContains) {
                lock.lock();
                try {
                    value = getReal(key);
                    if (value != null) {
                        memoryL1Cache.set(key, new SoftReference(value), new Date(System.currentTimeMillis() + l1Timeout));
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                value = (memoryL1Cache.get(key) == null) ? null : memoryL1Cache.get(key).get();
            }
        } else {
            value = getReal(key);
        }
        return value;
    }

    /**
     * 设置一级缓存的存活时间，超过时间，一级缓存将被清除
     *
     * @param timout   存活时间
     * @param timeUnit 时间单位
     */
    public void setL1CacheTimeout(int timout, TimeUnit timeUnit) {
        this.l1Timeout = timeUnit.toMillis(timout);
    }

    /**
     * 设置是否启用一级缓存
     *
     * @param enableL1Cache 是否启用一级缓存
     */
    public void setEnableL1Cache(boolean enableL1Cache) {
        this.enableL1Cache = enableL1Cache;
    }

    /**
     * 清除一级缓存
     */
    protected void clearL1Cache() {
        if (memoryL1Cache != null) {
            memoryL1Cache.clear();
        }
    }
}
