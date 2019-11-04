package com.dongzy.common.common;

/**
 * 定义一个系统级别的工具类，用于处理系统级别的信息
 */
public final class SystemUtils {

    /**
     * 操作系统枚举类
     */
    public enum OperatorSystemEnum {
        WINDOWS,
        MACOS,
        LINUX,
        OTHER
    }

    private static OperatorSystemEnum operatorSystemEnum;

    /**
     * 获取当前组件所运行的操作系统类型
     *
     * @return 操作系统类型
     */
    public static OperatorSystemEnum currentOperatorSystem() {
        if (operatorSystemEnum == null) {
            final String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("windows")) {
                operatorSystemEnum = OperatorSystemEnum.WINDOWS;
            } else if (osName.contains("linux")) {
                operatorSystemEnum = OperatorSystemEnum.LINUX;
            } else if (osName.contains("mac") && osName.contains("os")) {
                operatorSystemEnum = OperatorSystemEnum.MACOS;
            } else {
                operatorSystemEnum = OperatorSystemEnum.OTHER;
            }
        }
        return operatorSystemEnum;
    }

    private static int jdkVersion = -1;

    /**
     * 获取当前JDK的版本
     *
     * @return JDK版本, 如5, 6, 7, 8, 9等
     */
    public static int currentJdkVersion() {
        if (jdkVersion == -1) {
            final String version = System.getProperty("java.version");
            if (version.startsWith("1.4")) {
                jdkVersion = 4;
            } else if (version.startsWith("1.5")) {
                jdkVersion = 5;
            } else if (version.startsWith("1.6")) {
                jdkVersion = 6;
            } else if (version.startsWith("1.7")) {
                jdkVersion = 7;
            } else if (version.startsWith("1.8")) {
                jdkVersion = 8;
            } else if (version.startsWith("1.9")) {
                jdkVersion = 9;
            } else {
                throw new IllegalArgumentException("无法识别的版本信息：" + version);
            }
        }
        return jdkVersion;
    }

    /**
     * 获取当前剩余的内存数量
     *
     * @return 剩余可用内存，单位为MB
     */
    public static long freeMemory() {
        //剩余内存为：最大可用内存-已经申请内存+剩余可用内存
        long free = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory();
        return free / 1024 / 1024;
    }
}
