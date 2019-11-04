package com.dongzy.common.common;

/**
 * 变量名称处理工具，能够将字符串处理成符合要求的个各种用途的变量名称
 *
 * @author zouyong
 * @since JDK1.6
 */
public class VariableUtils {

    /**
     * 获取一个字符串作为类的字段定义时的表示形式，
     * 因为需要考虑大小写的问题，所以需要进行统一的处理
     *
     * @param value 需要格式化的字符串
     * @return 符合Java规范的字段变量名
     */
    public static String buildVariablesName(String value) {

        Validate.notBlank(value, "变量名称不能为空！");

        // 如果变量的第二个字母为大写字母，那么需要将前两个字母都变成小写，
        // 这个是Java的bean的要求，如字段为DPUserId,那么需要返回dpUserId，而不能返回dPUserId,因为后者在生成get和set方法是不符合java的bean的要求。
        if (value.length() > 1) {
            return value.substring(0, 2).toLowerCase() + value.substring(2);
        } else {
            return value.toLowerCase();
        }
    }

    /**
     * 获取一个字符串作为类的Get方法时的表示形式
     * 因为需要考虑大小写的问题，所以需要进行统一的处理
     *
     * @param value       需要格式化的字符串
     * @param booleanType 字段是否为boolean类型，针对boolean类型，需要做特殊处理
     * @return 因为需要考虑大小写的问题，所以需要进行统一的处理
     */
    public static String buildGetMethodName(String value, boolean booleanType) {
        value = buildVariablesName(value);
        if (booleanType) {        //如果是boolean类型
            if (matchBooleanName(value)) {
                return value;
            } else {
                return "is" + buildMethodName(value);
            }
        } else {
            return "get" + buildMethodName(value);
        }
    }

    /**
     * 获取一个字符串作为类的Set方法时的表示形式
     * 因为需要考虑大小写的问题，所以需要进行统一的处理
     *
     * @param value       需要格式化的字符串
     * @param booleanType 字段是否为boolean类型，针对boolean类型，需要做特殊处理
     * @return 因为需要考虑大小写的问题，所以需要进行统一的处理
     */
    public static String buildSetMethodName(String value, boolean booleanType) {
        value = buildVariablesName(value);
        //如果是boolean类型，并且采用is开头，那么
        if (booleanType && matchBooleanName(value)) {        //如果是boolean类型
            return "set" + buildMethodName(value).substring(2);
        } else {
            return "set" + buildMethodName(value);
        }
    }

    // 如果变量的第二个字母为大写字母，那么需要将前第二个字母变成小写，
    // 这个是Java的bean的要求，如字段为DPUserId,那么需要返回DpUserId，
    // 而不能返回dPUserId,因为后者在生成get和set方法是不符合java的bean的要求。
    public static String buildMethodName(String value) {
        if (value.length() > 1) {
            return value.substring(0, 1).toUpperCase() + value.substring(1);
        } else {
            value = value.toUpperCase();
        }
        return value;
    }

    /**
     * 判断属性名称是否为特殊的boolean类型，特殊boolean类型生成set和get的方法需要特殊处理
     *
     * @param value 字段名称
     * @return 是否为特殊boolena类型
     */
    private static boolean matchBooleanName(String value) {
        if (value.startsWith("is") && value.length() > 2) {     //以小写is开头，并且长度大于2
            char c = value.charAt(2);
            if (c >= 'A' && c <= 'Z') {     //第三个字母为大写字母，那么
                return true;
            }
        }
        return false;
    }
}
