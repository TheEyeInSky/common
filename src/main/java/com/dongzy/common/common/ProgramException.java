package com.dongzy.common.common;

/**
 * 程序执行异常
 * Created by 勇 on 2015/11/4.
 */
public class ProgramException extends BaseException {

    /**
     * 根据传入的参数构造函数
     *
     * @param message 自定义异常信息
     */
    public ProgramException(String message) {
        super(message);
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param message   异常信息
     * @param throwable 异常类
     */
    public ProgramException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param throwable 重新包装异常类
     */
    public ProgramException(Throwable throwable) {
        super(throwable);
    }
}
