package com.dongzy.common.mq;

import com.dongzy.common.common.Validate;
import com.dongzy.common.common.text.StringBuilderExt;
import com.dongzy.common.log.TextLoggerFactory;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 消息监听对象，用于监听一个队列的集合
 * 监听器会根据队列集合中队列的数量，创建与之数量匹配的线程，每个线程处理一个队列
 * <p>
 * 本类的适用场景：
 * 永不停止的循环往复的数据处理场景
 * 如：商城的订单队列，只要有新的订单产生，那么该订单将会被处理掉。
 * 如：实时处方审核，只要有新的审核任务，那么该处方将会被处理掉。
 * <p>
 * 不适用的场景：
 * 在所有对象处理完毕后，有后续任务需要执行的场景，因为很难判断是不是所有的数据是不是都已经处理完毕
 * 如：批量处方审核，并附带审核以后需要执行后续操作时，不适用。
 * 如：批量订单处理，并在订单全部处理完以后要触发其他事件，不适用。
 *
 * @author zouyong
 * @since JDK1.6
 */
public class QueueSetListener<T> {

    private final static Logger LOGGER = TextLoggerFactory.getInstance().getLogger(QueueSetListener.class);
    private QueueSet<T> queueSet;
    private Class<? extends QueueSetReceiver<T>> queueSetReceiverClass;
    private int groupSize = 1;                                          //每次从队列中取对象的数量，默认为每次取一个
    private volatile boolean abort = false;                           //用户是否触发了取消操作
    private volatile boolean busy = false;                            //是否已经启动监听程序；
    private AtomicLong processedNumber = new AtomicLong();              //记录监听器已经提交的对象数量
    private AtomicLong processedSuccessNumber = new AtomicLong();       //记录监听器已经成功处理的对象数量
    private Thread[] threads;
    private boolean autoClose = false;                                //是否在监听队列为为空时自动关闭监听器
    private Lock lock = new ReentrantLock();                            //线程锁

    public QueueSetListener() {
    }

    /**
     * 设置是否在监听队列为为空时自动关闭监听器
     *
     * @param autoClose 是否在监听队列为为空时自动关闭监听器
     */
    public void setAutoClose(boolean autoClose) {
        this.autoClose = autoClose;
    }

    /**
     * 获取监听器已经提交的对象数量
     *
     * @return 监听器已经提交的对象数量
     */
    public long getProcessedNumber() {
        return processedNumber.get();
    }

    /**
     * 获取监听器已经成功处理的对象数量
     *
     * @return 监听器已经成功处理的对象数量
     */
    public long getProcessedSuccessNumber() {
        return processedSuccessNumber.get();
    }

    /**
     * 设置每次从队列中取出的元素对象的数量
     */
    public void setGroupSize(int groupSize) {
        if (!busy) {
            this.groupSize = groupSize;
        }
    }

    /**
     * 获取指定状态的线程数量
     *
     * @param states 线程的状态
     * @return 线程数量
     */
    public int getThreadNumber(Thread.State... states) {
        int count = 0;
        if (states != null && threads != null) {
            for (Thread thread : threads) {
                for (Thread.State state : states) {
                    if (thread.getState() == state) {
                        count++;
                        break;
                    }
                }
            }
        }
        return count;
    }

    /**
     * 设置监听队列
     *
     * @param queueSet 监听队列
     */
    public void setQueueSet(QueueSet<T> queueSet) {
        if (!busy) {
            Validate.notNull(queueSet, "queueSet对象不能为null。");
            this.queueSet = queueSet;
        }
    }

    /**
     * 设置队列中对象的处理程序
     *
     * @param queueReceiverClass 回调类
     */
    public void setQueueReceiver(Class<? extends QueueSetReceiver<T>> queueReceiverClass) {
        if (!busy) {
            Validate.notNull(queueReceiverClass, "queueReceiverClass对象不能为null。");
            this.queueSetReceiverClass = queueReceiverClass;
        }
    }

    /**
     * 开始监听处理进度
     */
    synchronized public void start() {
        if (busy) {
            LOGGER.warn("无法重复启动监听器，处理程序为：" + queueSetReceiverClass);
            return;
        }

        busy = true;

        processedNumber.set(0);
        processedSuccessNumber.set(0);

        int queueCount = queueSet.getSubQueues().size();

        if (queueCount == 0) {
            busy = false;
            throw new IllegalArgumentException("队列集合中无任何队列对象，请先添加队列对象");
        }

        threads = new Thread[queueCount];

        for (int i = 0; i < queueCount; i++) {
            IQueue<T> queue = queueSet.getSubQueues().get(i);
            threads[i] = new Thread() {
                @Override
                public void run() {
                    try {
                        QueueSetReceiver<T> receiver = queueSetReceiverClass.newInstance();
                        while (true) {
                            if (abort) {          //如果终止了监听，那么就退出循环
                                return;
                            }

                            Collection<T> objectsCollection = queue.poll(groupSize);

                            final int collectionSize = objectsCollection.size();
                            if (collectionSize > 0) {
                                processedNumber.getAndAdd(collectionSize);
                                try {
                                    int count = receiver.receive(objectsCollection);
                                    processedSuccessNumber.getAndAdd(count);
                                } catch (Exception ex) {
                                    LOGGER.error("从待处理队列获取数据发生异常！", ex);
                                }
                            } else {
                                if (autoClose) {           //如果开启了自动关闭功能，那么当队列没有内容，就自动退出当前线程。
                                    return;
                                } else {
                                    Thread.yield();
                                }
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("处理监听队列时发生异常！", e);
                    } finally {
                        lock.lock();
                        try {
                            if (getThreadNumber(State.TERMINATED, State.NEW) >= threads.length - 1) {      //如果所有的线程都处于运行完毕的状态，那么监听器处于休息状态，可以启动监听。
                                busy = false;
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            };
        }

        for (int i = 0; i < queueCount; i++) {
            threads[i].start();
        }

        recordWorkLog();
    }

    /**
     * 获取当前的监听器是否处于忙的状态
     *
     * @return 监听器是否在工作状态
     */
    public boolean isBusy() {
        return busy;
    }

    /**
     * 设置取消标识为true
     */
    public void stop() {
        abort = true;
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    //如果所有的线程都处于运行完毕的状态，那么监听器处于休息状态，可以启动监听。
                    if (getThreadNumber(Thread.State.TERMINATED, Thread.State.NEW) >= threads.length) {
                        busy = false;
                        return;
                    }
                    Thread.yield();
                }
            }
        }.start();
    }

    /**
     * 记录监听器的工作日志
     */
    private void recordWorkLog() {
        new Thread() {
            @Override
            public void run() {
                long lastChangedTime = System.currentTimeMillis();
                while (busy) {
                    long newTime = System.currentTimeMillis();
                    if (newTime - lastChangedTime >= 60000) {      //如果达到了60秒了，那么记录一次日志。
                        lastChangedTime = newTime;
                        StringBuilderExt stringBuilder = new StringBuilderExt(200);
                        stringBuilder.appendFormat("Receiver Class:[{0}],", queueSetReceiverClass);
                        stringBuilder.appendFormat("completed:[{0}],", processedNumber.get());
                        stringBuilder.appendFormat("successed:[{0}],", processedSuccessNumber.get());
                        stringBuilder.appendFormat("active threads:[{0}],", getThreadNumber(State.RUNNABLE));
                        stringBuilder.appendFormat("wait(block) threads:[{0}],", getThreadNumber(State.WAITING, State.TIMED_WAITING, State.BLOCKED));
                        stringBuilder.appendFormat("other threads:[{0}].", getThreadNumber(State.NEW, State.TERMINATED));
                        LOGGER.info(stringBuilder.toString());
                    }
                    Thread.yield();
                }
            }
        }.start();
    }
}
