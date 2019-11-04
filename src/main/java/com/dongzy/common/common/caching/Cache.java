package com.dongzy.common.common.caching;

import com.dongzy.common.common.Validate;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        Validate.notNull(keys, "获取缓存的keys不能为空！");

        Map<String, T> newMap = new HashMap<>();
        for (String key : keys) {
            if (key != null) {
                newMap.put(key, get(key));
            }
        }
        return newMap;
    }
}
