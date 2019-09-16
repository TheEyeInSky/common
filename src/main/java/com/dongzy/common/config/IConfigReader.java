package com.dongzy.common.config;

import java.util.Collection;

/**
 * 数据库配置项读写辅助类
 */
public interface IConfigReader {

    /**
     * 获取指定namespace的全部配置项
     *
     * @param namespace 命名空间
     * @param appCode   app编码
     * @return 子节点的集合
     */
    Collection<ConfigItemInfo> getConfigItems(String namespace, String appCode);
}
