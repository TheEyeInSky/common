package com.dongzy.common.common;

/**
 * 数据格式异常
 */
public class DataFormatException extends Exception {

	/**
	 * 根据传入的参数构造函数
	 *
	 * @param message 自定义异常信息
	 */
	public DataFormatException(String message) {
		super(message);
	}

	/**
	 * 根据传入的参数构造函数
	 *
	 * @param message   异常信息
	 * @param throwable 异常类
	 */
	public DataFormatException(String message, Throwable throwable) {
		super(message, throwable);
	}

	/**
	 * 根据传入的参数构造函数
	 *
	 * @param throwable 重新包装异常类
	 */
	public DataFormatException(Throwable throwable) {
		super(throwable);
	}
}
