package com.dongzy.common.common.caching;

import com.dongzy.common.common.Validate;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Date;

/**
 * 内存缓存的实现类，主要包含以下功能：
 * 1、定义了垃圾回收机制，会定期回收不再使用的缓存内容
 * 2、为满足内存缓存模式，重写了一些基础类的读写缓存方法
 *
 * @author zouyong
 * @since JDK1.5
 */
public final class MethodResultCache<T> extends CacheByL1Cache<T> {

    private final IMethodResult<T> methodResult;

    /**
     * 根据传入的参数构造函数
     *
     * @param methodResult 获取指定key值得方法
     */
    public MethodResultCache(IMethodResult<T> methodResult) {
        this.methodResult = methodResult;
        setEnableL1Cache(true);         //默认开启一级缓存
    }

    @Override
    public void set(String key, T value) {
        set(key, value, null);
    }

    @Override
    public void set(String key, T value, Date date) {
        Validate.notNull(key,"缓存key不能为空");
        memoryL1Cache.set(key, new SoftReference(value), date);
    }

    @Override
    protected T getReal(String key) {
        return methodResult.get(key);
    }

    @Override
    synchronized public void remove(String key) {
        Validate.notNull(key,"缓存key不能为空");
        memoryL1Cache.remove(key);
    }

    @Override
    synchronized public void remove(Collection<String> keys) {
        for (String key : keys) {
            remove(key);
        }
    }

    @Override
    synchronized public void clear() {
        clearL1Cache();
    }

    @Override
    public boolean containsKey(String key) {
        return methodResult.containsKey(key);
    }
}
