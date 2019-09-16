package com.dongzy.common.mq;

import com.dongzy.common.common.Validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 内存队列实现类，采用内存的方式来保存数据
 *
 * @author zouyong
 * @since JDK1.6
 */
public class MemoryQueue<T> implements IQueue<T> {

    //默认的队列元素最大值
    private static final int DEFAULT_CAPACITY = 2000;
    private final Queue<T> queue;

    /**
     * 默认构造器
     */
    public MemoryQueue() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * 指定容器的大小进行构造
     *
     * @param capacity 队列的最大容量
     */
    public MemoryQueue(int capacity) {
        queue = new LinkedBlockingQueue<>(capacity);
    }

    @Override
    public boolean add(T t) {
        Validate.notNull(t);
        return queue.add(t);
    }

    @Override
    public boolean add(Collection<T> collection) {
        for (T t : collection) {
            add(t);
        }
        return true;
    }

    @Override
    public T poll() {
        return queue.poll();
    }

    @Override
    public Collection<T> poll(final int number) {
        Collection<T> collection = new ArrayList<>(number);
        for (int i = 0; i < number; i++) {
            T t = this.poll();
            if (t != null) {
                collection.add(t);
            } else {        //如果队列为空了，那么直接跳出循环
                break;
            }
        }
        return collection;
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public long size() {
        return queue.size();
    }

    @Override
    public synchronized void clear() {
        queue.clear();
    }
}
