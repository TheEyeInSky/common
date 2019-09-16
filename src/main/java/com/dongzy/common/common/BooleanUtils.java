package com.dongzy.common.common;

import com.dongzy.common.common.text.StringUtils;

/**
 * <p>用于进行布尔类型转换的辅助类</p>
 * <p>能够将各种类型的数据转换为布尔类型的数据</p>
 * <p>能够将布尔类型转换成其他数据类型的值</p>
 *
 * @author zouyong
 * @since JDK1.0
 */
public final class BooleanUtils {

    /**
     * 将字符串类型那个转换为boolean类型
     * 如果内容为空，那么返回空值
     * 如果内容不为空时，转换正常时返回正常boolean值，转换失败时返回false
     *
     * @param string 需要转换成Boolean的字符串
     * @return Boolean对象
     */
    public static Boolean tryToBoolean(String string) {
        try {
            return toBoolean(string);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * 将字符串类型那个转换为boolean类型
     * 如果内容为空，那么返回空值
     * 如果内容不为空时，转换正常时返回正常boolean值，转换失败时返回false
     *
     * @param string 需要转换成Boolean的字符串
     * @return Boolean对象
     */
    public static Boolean toBoolean(String string) {
        if (StringUtils.isBlank(string)) {
            return null;           //空白字符窜为false值
        }

        string = string.trim().toLowerCase();
        switch (string) {
            case "1":
            case "true":
            case "yes":
                return true;
            case "0":
            case "false":
            case "no":
                return false;
            default:
                throw new IllegalArgumentException("无法识别的boolean类型值：" + string);
        }
    }

    /**
     * 将字符串类型那个转换为boolean类型
     *
     * @param string 需要转换成Boolean的字符串
     * @param value  默认值
     * @return boolean对象
     */
    public static boolean toBoolean(String string, boolean value) {
        try {
            Boolean bln = toBoolean(string);
            return (bln == null) ? value : bln;
        } catch (IllegalArgumentException ex) {
            return value;
        }
    }

    /**
     * 将字符串类型那个转换为boolean类型
     *
     * @param value 需要转换成Boolean的数字
     * @return boolean对象
     */
    public static boolean toBoolean(int value) {
        if (value == 1) {
            return true;
        } else if (value == 0) {
            return false;
        } else {
            throw new IllegalArgumentException("无法识别的boolean类型值：" + value);
        }
    }

    /**
     * 将Boolean类型的变量转换为字符串类型,True返回1，False返回0,Null返回空
     *
     * @param bln 布尔类型
     * @return 字符串类型
     */
    public static String toString(Boolean bln) {
        if (bln == null) {
            return null;
        }

        return (bln) ? "1" : "0";
    }

    /**
     * 将Boolean类型的变量转换为数字类型,True返回1，False返回0,Null返回空
     *
     * @param bln 布尔类型
     * @return 字符串类型
     */
    public static Integer toInteger(Boolean bln) {
        if (bln == null) {
            return null;
        }

        return (bln) ? 1 : 0;
    }
}
