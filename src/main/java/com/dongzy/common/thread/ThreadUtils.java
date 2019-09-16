package com.dongzy.common.thread;

import java.util.concurrent.TimeUnit;

/**
 * 线程辅助工具
 *
 * @author zouyong
 * @since SDK1.6a
 */
public class ThreadUtils {

    /**
     * 让线程休息指定的时间
     *
     * @param millis 休息指定的时间
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis < 0 ? 0 : millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();     //设置线程为中断状态。
            e.printStackTrace();
        }
    }

    /**
     * 让线程休息指定的时间
     *
     * @param time 休息指定的时间
     * @param unit 休息时间的单位
     */
    public static void sleep(long time, TimeUnit unit) {
        try {
            time = time < 0 ? 0 : time;
            long millis = unit.toMillis(time);
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();     //设置线程为中断状态。
            e.printStackTrace();
        }
    }
}
