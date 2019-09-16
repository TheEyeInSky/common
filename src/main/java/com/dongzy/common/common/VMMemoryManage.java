package com.dongzy.common.common;

/**
 * 自定义虚拟机内存管理类
 *
 * @author zouyong
 * @since JDK1.0
 */
public class VMMemoryManage {

    /**
     * 尝试让JVM回收内存
     */
    public static void gc() {
        System.gc();
        Runtime.getRuntime().gc();
    }

}
