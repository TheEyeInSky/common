package com.dongzy.common.mq;

import java.util.Collection;

/**
 * 队列对象的接口定义
 * 队列对象的基本特征是先进先出
 *
 * @author zouyong
 * @since JDK1.6
 */
public interface IQueue<T> {

    /**
     * 添加一个对象到队列中
     *
     * @param e 需要添加到队列中的对象
     * @throws IllegalStateException 如果超过队列长度限制会抛出此异常
     * @throws NullPointerException  如果插入的值为null，会抛出异常
     */
    boolean add(T e);

    /**
     * 添加对象集合到队列中
     *
     * @param collection 需要添加到队列中的对象集合
     * @throws IllegalStateException 如果超过队列长度限制会抛出此异常
     * @throws NullPointerException  如果插入的值为null，会抛出异常
     */
    boolean add(Collection<T> collection);

    /**
     * 从队列中获取对象，如果队列为空，那么将返回NULL
     *
     * @return 队列中的对象
     */
    T poll();

    /**
     * 从队列中获取对象，如果队列为空，那么返回的集合对象个数为0
     *
     * @param number 获取的数量
     * @return 队列中的对象
     */
    Collection<T> poll(int number);

    /**
     * 判断队列是否为空
     *
     * @return 是否为空
     */
    boolean isEmpty();

    /**
     * 队列中元素的个数
     *
     * @return 元素的个数
     */
    long size();

    /**
     * 清空队列对象
     */
    void clear();
}
