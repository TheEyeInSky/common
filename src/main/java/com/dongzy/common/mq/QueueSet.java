package com.dongzy.common.mq;

import com.dongzy.common.log.TextLoggerFactory;
import org.slf4j.Logger;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 队列集对象，包含一个多个标准的队列对象，如果往队列集合中插入多个元素，会自动平分到各个子队列中
 *
 * @author zouyong
 * @since JDK1.6
 */
public class QueueSet<T> {

    private final static Logger LOGGER = TextLoggerFactory.getInstance().getLogger(QueueSet.class);
    private List<IQueue<T>> subQueues;
    private Class<? extends IQueue<T>> queueClass;
    private int index = 0;
    private int queueTotal = 1;
    private int maxCapacity = 20_000;       //队列容量的最大值（本值只是一个大概的范围，不一定等于实际的最大大小）

    /**
     * 根据传入的参数构造函数
     *
     * @param queueClass 子队列的实现类类型
     */
    public QueueSet(Class<? extends IQueue<T>> queueClass) {
        this.queueClass = queueClass;
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param queues 子队列集合
     * @throws InterruptedException 子队列为空时抛出的异常
     */
    public QueueSet(IQueue<T>... queues) throws InterruptedException {
        if (queues == null) {
            throw new InterruptedException("队列对象不能为空！");
        }
        subQueues = new ArrayList<>(queues.length);
        for (IQueue<T> queue : queues) {
            subQueues.add(queue);
        }
    }

    /**
     * 默认构造函数
     *
     * @param queueClass 子队列的实现类类型
     * @param queueTotal 子队列的数量
     */
    public QueueSet(Class<? extends IQueue<T>> queueClass, int queueTotal) {
        this.queueClass = queueClass;
        this.queueTotal = queueTotal;
    }

    /**
     * 默认构造函数
     *
     * @param queueClass  子队列的实现类类型
     * @param queueTotal  子队列的数量
     * @param maxCapacity 所有队列元素的上限
     */
    public QueueSet(Class<? extends IQueue<T>> queueClass, int queueTotal, int maxCapacity) {
        this.queueClass = queueClass;
        this.queueTotal = queueTotal;
        this.maxCapacity = maxCapacity;
    }

    /**
     * 获取队列集合容量的最大值，是一个近似值
     * 因为为了保证性能，不可能做到每插入一个对象，检查一次总数，
     *
     * @return 队列集合容量的最大值
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * 获取子队列的集合
     *
     * @return 子队列集合
     */
    public List<IQueue<T>> getSubQueues() {
        if (subQueues == null) {
            int size = maxCapacity / queueTotal;
            subQueues = new ArrayList<>(queueTotal);
            for (int i = 0; i < queueTotal; i++) {
                IQueue<T> queue;
                try {
                    Constructor<? extends IQueue<T>> constructor = queueClass.getDeclaredConstructor(new Class[]{int.class});
                    queue = constructor.newInstance(size);
                    subQueues.add(queue);
                } catch (Exception e) {
                    //TODO 忽略异常内容
                }
            }
        }
        return subQueues;
    }

    /**
     * 获取队列集合中所有队列元素的总和
     *
     * @return 元素个数的总和
     */
    public synchronized long size() {
        long longValue = 0;
        for (IQueue<T> queue : getSubQueues()) {
            longValue += queue.size();
        }
        return longValue;
    }

    /**
     * 添加对象到队列集合中
     *
     * @param item 需要添加的对象
     */
    public void put(T item) {
        if (item != null) {
            Collection<T> collection = new ArrayList<>(1);
            collection.add(item);
            put(collection);
        }
    }

    /**
     * 添加多个对象到队列集合中
     *
     * @param items 需要添加的对象集合
     */
    public synchronized void put(Collection<T> items) {
        try {
            T item;
            final int size = items.size() / queueTotal + 1;
            final Iterator<T> iterator = items.iterator();     //获取迭代器

            for (int i = 0; i < queueTotal; i++) {      //遍历所有的子队列
                IQueue<T> queue = getSubQueues().get(index);
                for (int j = 0; j < size; j++) {
                    if (iterator.hasNext()) {
                        item = iterator.next();
                        if (item != null) {
                            queue.add(item);
                        }
                    } else {
                        return;
                    }
                }
                index = (index > queueTotal - 2) ? 0 : index + 1;
            }
        } catch (Exception e) {
            LOGGER.error("将对象添加到队列时发生异常！", e);
        }
    }

    /**
     * 清空所有队列的对象
     */
    public synchronized void clear() {
        getSubQueues().forEach(IQueue::clear);
    }

}
