package com.dongzy.common.common;

import com.dongzy.common.common.text.StringUtils;

import java.text.MessageFormat;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 用于UUID相关的辅助类，主要包括以下功能
 * 1、将字符串转换为合法的UUID
 * 2、将UUID转换为字符串
 * 3、保证线程安全的情况下，生成一个随机的UUID。
 *
 * @author zouyong
 * @since JDK1.6
 */
public final class UuidUtils {

    private static Queue<UUID> uuidQueue = new ConcurrentLinkedQueue<>();
    private static final int QUEUE_SIZE = 100_000;      //UUID队列的大小
    private static Lock lock = new ReentrantLock();

    /**
     * 获取ＵＵＩＤ的值
     *
     * @param string 需要转换成UUID的字符串
     * @return UUID对象
     */
    public static UUID toUUID(String string) {
        if (string != null) {
            string = string.trim();
            if (string.length() == 32) {
                string = MessageFormat.format("{0}-{1}-{2}-{3}-{4}", string.substring(0, 8),
                        string.substring(8, 12), string.substring(12, 16), string.substring(16, 20), string.substring(20));
            }
        }
        return StringUtils.isBlank(string) ? null : UUID.fromString(string);
    }

    /**
     * 获取uuid的字符串表示
     *
     * @param uuid UUID对象
     * @return UUID转换后的字符串
     */
    public static String toString(UUID uuid) {
        return (uuid == null) ? null : uuid.toString();
    }

    /**
     * 获得线程安全的UUID对象
     *
     * @return 新的UUID对象
     */
    public static UUID buildUUID() {
        if (uuidQueue.isEmpty()) {
            lock.lock();
            try {
                if (uuidQueue.isEmpty()) {
                    for (int i = 0; i < QUEUE_SIZE; i++) {
                        uuidQueue.add(UUID.randomUUID());
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        return uuidQueue.poll();
    }
}
