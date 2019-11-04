package com.dongzy.common.common;

import java.io.IOException;


/**
 * 获取数据时发生异常
 *
 * @author 勇
 */
public class DataGetException extends IOException {

    /**
     * 根据传入的参数构造函数
     *
     * @param message 自定义异常信息
     */
    public DataGetException(String message) {
        super(message);
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param message   异常信息
     * @param throwable 异常类
     */
    public DataGetException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param throwable 重新包装异常类
     */
    public DataGetException(Throwable throwable) {
        super(throwable);
    }
}
