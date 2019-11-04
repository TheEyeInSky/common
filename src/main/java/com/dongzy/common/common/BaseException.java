package com.dongzy.common.common;

/**
 * 自定义异常的基础类，定义了自定义异常的基本特性
 * <p>
 * 因为{@code BaseException}继承自{@code RuntimeException}类，所以会包含一个<em>未受检的异常</em>
 * </p>
 *
 * @author zouyong
 * @since JDK1.0
 */
public class BaseException extends RuntimeException {

    /**
     * 根据传入的参数构造函数
     *
     * @param message 自定义异常信息
     */
    public BaseException(String message) {
        super(message);
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param message   异常信息
     * @param throwable 异常类
     */
    public BaseException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param throwable 重新包装异常类
     */
    public BaseException(Throwable throwable) {
        super(throwable);
    }

}
