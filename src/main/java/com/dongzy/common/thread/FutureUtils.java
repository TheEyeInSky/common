package com.dongzy.common.thread;

import java.util.concurrent.*;

/**
 * 用于辅助判断执行时间的类
 */
public class FutureUtils {

    //初始化一个单线程工具
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    private static int timeout = 5000;

    /**
     * 设置默认的执行方法超时时间
     *
     * @param timeout 超时时间，单位毫秒
     */
    public static void setTimeout(int timeout) {
        FutureUtils.timeout = timeout;
    }

    /**
     * 执行指定的方法，如果在指定的时间内没有返回值，那么将返回默认的值
     *
     * @param callable     执行的方法
     * @param defaultValue 超时返回的默认值
     */
    public static <T> T execute(Callable<T> callable, T defaultValue) {
        return execute(callable, timeout, defaultValue);
    }

    /**
     * 执行指定的方法，如果在指定的时间内没有返回值，那么将返回默认的值
     *
     * @param callable     执行的方法
     * @param timeout      超时的时间，单位毫秒
     * @param defaultValue 超时返回的默认值
     */
    public static <T> T execute(Callable<T> callable, int timeout, T defaultValue) {

        RunnableFuture<T> futureTask = new FutureTask<T>(callable);
        try {
            EXECUTOR_SERVICE.submit(futureTask);
            return futureTask.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            futureTask.cancel(true);
            e.printStackTrace();
            return defaultValue;
        }
    }
}
