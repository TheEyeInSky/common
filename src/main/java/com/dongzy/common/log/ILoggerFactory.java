package com.dongzy.common.log;

import org.slf4j.Logger;

/**
 * 日记记录类的接口类
 *
 * @author zouyong
 * @since JDK1.0
 */
public interface ILoggerFactory extends org.slf4j.ILoggerFactory {

    /**
     * 获取log的上下文名称
     *
     * @return 上下文名称
     */
    String getContextName();

    /**
     * 获取日志记录器
     *
     * @param clazz 产生日志的类类型
     * @return 日志记录器
     */
    Logger getLogger(final Class<?> clazz);

    /**
     * 获取日志记录器
     *
     * @param contextName 记录日志的上下文名称，会体现在记录介质的名称上
     * @param clazz       产生日志的类类型
     * @return 日志记录器
     */
    Logger getLogger(String contextName, final Class<?> clazz);

    /**
     * 获取是否对日志进行分类记录
     *
     * @return 是否对日志进行分类记录
     */
    boolean isSplitFile();

    /**
     * 设置是否对日志进行分类记录
     *
     * @param splitFile 是否分类记录
     */
    void setSplitFile(boolean splitFile);

    /**
     * 设置日志保留的最大天数
     *
     * @param maxHistory 保留的最大天数
     */
    void setMaxHistory(int maxHistory);

    /**
     * 获取日志保留的最大天数
     *
     * @return 日志保留的最大天数
     */
    int getMaxHistory();

}
