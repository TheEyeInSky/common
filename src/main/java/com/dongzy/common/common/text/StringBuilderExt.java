package com.dongzy.common.common.text;

import java.text.MessageFormat;

/**
 * 自主开发的StringBuilder对象，新增了以下主要方法
 * 添加内容并再内容的末尾增加换行符
 * 支持直接添加占位符模式的字符串添加
 * 支持直接添加换行符等
 *
 * @author zouyong
 * @since JDK1.6
 */
public class StringBuilderExt {

    private StringBuilder stringBuilder;

    /**
     * 默认构造函数
     */
    public StringBuilderExt() {
        stringBuilder = new StringBuilder();
    }

    /**
     * 创建指定初始化大小的StringBuilder对象
     *
     * @param capacity 初始化大小
     */
    public StringBuilderExt(int capacity) {
        stringBuilder = new StringBuilder(capacity);
    }

    /**
     * 获取对象的初始空间大小
     *
     * @return 初始空间大小
     */
    public int capacity() {
        return stringBuilder.capacity();
    }

    /**
     * 在末尾增加新的内容
     *
     * @param object 需要添加的额内容
     * @return StringBuilderExt对象
     */
    public StringBuilderExt append(Object object) {
        stringBuilder.append(object);
        return this;
    }

    /**
     * 添加一个空行
     *
     * @return StringBuilderExt对象
     */
    public StringBuilderExt appendLine() {
        stringBuilder.append(StringUtils.LINE_SPEARATOR);
        return this;
    }

    /**
     * 添加一个对象，并添加一个换行
     *
     * @param object 需要添加的对象
     * @return StringBuilderExt对象
     */
    public StringBuilderExt appendLine(Object object) {
        stringBuilder.append(object);
        stringBuilder.append(StringUtils.LINE_SPEARATOR);
        return this;
    }

    /**
     * 按照messageformat的格式输入内容
     *
     * @param format  messagefomart的格式字符串
     * @param objects 添加内容对象集合
     * @return StringBuilderExt对象
     */
    public final StringBuilderExt appendFormat(String format, Object... objects) {
        stringBuilder.append(MessageFormat.format(format, objects));
        return this;
    }

    /**
     * 按照messageformat的格式输入内容，并添加换行
     *
     * @param format  messagefomart的格式字符串
     * @param objects 对象集合
     * @return StringBuilderExt对象
     */
    public final StringBuilderExt appendFormatLine(String format, Object... objects) {
        stringBuilder.append(MessageFormat.format(format, objects));
        stringBuilder.append(StringUtils.LINE_SPEARATOR);
        return this;
    }

    /**
     * 设置对象的长度，超出的部分将被截取
     *
     * @param newLength 新的长度
     * @return StringBuilderExt对象
     */
    public StringBuilderExt setLength(int newLength) {
        stringBuilder.setLength(newLength);
        return this;
    }

    /**
     * 获取对象的长度
     *
     * @return 对象的长度
     */
    public int length() {
        return stringBuilder.length();
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }

}
