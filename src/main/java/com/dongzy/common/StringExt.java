package com.dongzy.common;

/**
 * @author dongzy
 * @Desc
 * @date 2019/9/16.
 */

import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import java.util.UUID;

/**
 * 字符串处理类，主要包含以下功能：
 * 1、判断字符串是否为空串
 * 2、连接字符串的方法
 * 3、去掉字符串首尾指定字符串（如空格等）的方法
 * 4、生成随机字符串的方法
 * 5、获取指定宽度字符串的方法
 *
 * @author zouyong
 * @since JDK1.6
 */
public final class StringExt {

    public enum HorizontalAlignment {
        LEFT,
        CENTER,
        RIGHT
    }

    /**
     * 返回一个空的字符串
     */
    public final static String EMPTY = "";
    /**
     * 换行符
     */
    public final static String LINE_SPEARATOR = System.getProperty("line.separator");
    /**
     * trim默认串
     */
    public final static char[] TRIM_DEFAULT_CHARS = new char[]{' ', '\n', '\r', '\t'};

    /**
     * 判断字符串是否为空串，同时如果字符串中的字母全部为空格字符，也会返回true
     *
     * @param value 需要判断的字符串
     * @return 字符串是否为空
     */
    public static boolean isNullOrWhiteSpace(String value) {
        return value == null || value.trim().equals(EMPTY);
    }

    /**
     * 判断字符串是否为空串，同时如果字符串没有任何字符，也会返回true
     *
     * @param value 需要判断的字符串
     * @return 字符串是否为空
     */
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.equals(EMPTY);
    }

    /**
     * 采用指定的连接字符串，将一组字符连接起来，比如用逗号连接一个字符串数组等。
     * 如果字符串数组中只有一个元素，那么将不会添加连接字符串
     *
     * @param originalString 用于连接的字符串
     * @param strings        需要连接起来的字符串集合
     * @return 连接完成的字符串
     */
    public static String join(String originalString, String... strings) {
        if (strings.length == 0) {
            return EMPTY;
        }
        boolean isFirst = true;
        StringBuilder sBuilder = new StringBuilder(50);
        for (String string : strings) {
            if (isFirst)
                isFirst = false;
            else
                sBuilder.append(originalString);
            sBuilder.append(string);
        }
        return sBuilder.toString();
    }

    /**
     * 去掉指定字符串的首尾指定字符，比如去掉字符串首尾的单引号和双引号，那么格式如下：
     * trim(strValue, '\'','\"');
     *
     * @param string    需要处理的字符串
     * @param trimChars 需要移除的字符集合
     * @return 字符串
     */
    public static String trim(String string, char... trimChars) {

        if (string == null) {
            return null;
        }

        if (trimChars == null || trimChars.length == 0) {
            trimChars = TRIM_DEFAULT_CHARS;
        }

        int start = 0;
        int end = string.length() - 1;
        char[] chars = string.toCharArray();

        for (; start < string.length(); start++) {
            char ch = chars[start];
            int i = 0;
            for (; i < trimChars.length; i++) {
                if (trimChars[i] == ch)
                    break;
            }
            if (i == trimChars.length)
                break;
        }

        for (; end >= start; end--) {
            char ch = chars[end];
            int i = 0;
            for (; i < trimChars.length; i++) {
                if (trimChars[i] == ch)
                    break;
            }
            if (i == trimChars.length)
                break;
        }

        int len = end - start + 1;
        if (len == string.length()) {
            return string;
        }
        if (len == 0) {
            return EMPTY;
        }
        return string.substring(start, start + len);
    }

    /**
     * 去掉指定字符串的首尾指定字符，比如去掉字符串首尾的单引号和双引号，那么格式如下：
     * trim(strValue, '\'','\"');
     *
     * @param string    需要处理的字符串
     * @param trimChars 需要移除的字符集合
     * @return 字符串
     */
    public static String trimLeft(String string, char... trimChars) {

        if (trimChars == null || trimChars.length == 0) {
            trimChars = TRIM_DEFAULT_CHARS;
        }

        int start = 0;
        int end = string.length() - 1;
        char[] chars = string.toCharArray();

        for (; start < string.length(); start++) {
            char ch = chars[start];
            int i = 0;
            for (; i < trimChars.length; i++) {
                if (trimChars[i] == ch)
                    break;
            }
            if (i == trimChars.length)
                break;
        }

        int len = end - start + 1;
        if (len == string.length()) {
            return string;
        }
        if (len == 0) {
            return EMPTY;
        }
        return string.substring(start, start + len);
    }

    /**
     * 去掉指定字符串的尾部指定字符，比如去掉字符串首尾的单引号和双引号，那么格式如下：
     * trim(strValue, '\'','\"');
     *
     * @param string    需要处理的字符串
     * @param trimChars 需要移除的字符集合
     * @return 字符串
     */
    public static String trimRight(String string, char... trimChars) {

        if (trimChars == null || trimChars.length == 0) {
            trimChars = TRIM_DEFAULT_CHARS;
        }

        int end = string.length() - 1;
        char[] chars = string.toCharArray();

        for (; end >= 0; end--) {
            char ch = chars[end];
            int i = 0;
            for (; i < trimChars.length; i++) {
                if (trimChars[i] == ch)
                    break;
            }
            if (i == trimChars.length)
                break;
        }

        int len = end + 1;
        if (len == string.length()) {
            return string;
        }
        if (len == 0) {
            return EMPTY;
        }
        return string.substring(0, len);
    }

    /**
     * 生成随机字符串
     *
     * @param stringLength     生成字符串长度
     * @param randomStringType 字符串内容组成
     * @return 字符串
     */
    public static String buildRandomString(int stringLength, RandomStringType randomStringType) {
        char[] numbers = new char[]{'2', '3', '4', '5', '6', '7', '8', '9'};
        char[] letters = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

        StringBuilder sb = new StringBuilder(stringLength);
        for (int i = 0; i < stringLength; i++) {
            Random random = new Random(UUID.randomUUID().hashCode());

            switch (randomStringType) {
                case NumberAndLetter:
                    if (i % 2 == 0) {
                        sb.append(numbers[random.nextInt(numbers.length)]);
                    } else {
                        sb.append(letters[random.nextInt(letters.length)]);
                    }
                    break;
                case OnlyLetter:
                    sb.append(letters[random.nextInt(letters.length)]);
                    break;
                case OnlyNumber:
                    sb.append(numbers[random.nextInt(numbers.length)]);
                    break;
            }
        }

        return sb.toString();
    }

    /**
     * 将指定字符串根据传入的表达式拆分成字符串集合，并自动移除空项。
     *
     * @param content 需要拆分的内容
     * @param regex   分隔符表达式
     * @param isTrim  是去掉每个拆分出来的串的首尾空格
     * @return 拆分结果集合
     */
    public static Collection<String> splitAndRemoveEmpty(String content, String regex, boolean isTrim) {
        Collection<String> strCollection = new LinkedList<>();
        String[] strValues = content.split(regex);
        for (String strValue : strValues) {
            if (!isNullOrWhiteSpace(strValue)) {
                if (isTrim) {
                    strCollection.add(strValue.trim());
                } else {
                    strCollection.add(strValue);
                }
            }
        }
        return strCollection;
    }

    /**
     * 获取固定宽度的字符串，如果字符串宽度过长，会被截断，如果字符串宽度不够，会居中显示
     *
     * @param message 需要显示的内容
     * @param width   内容所占的宽度
     * @return 字符串
     */
    public static String getStringByFixWidth(String message, int width) {
        return getStringByFixWidth(message, width, HorizontalAlignment.CENTER);
    }

    /**
     * 获取固定宽度的字符串，如果字符串宽度过长，会被截断，如果字符串宽度不够,会按照指定的对齐方式对齐
     *
     * @param message   需要显示的内容
     * @param width     内容所占的宽度
     * @param alignment 水平对齐方式
     * @return 字符串
     */
    public static String getStringByFixWidth(String message, int width, HorizontalAlignment alignment) {
        char[] chars = new char[width];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = ' ';
        }
        if (message == null) {
            message = "null";
        }

        String string = message.trim();
        if (string.length() > width) {            //如果字符串长度大于宽度,字符串将会被截取
            for (int i = 0; i < width; i++) {
                chars[i] = string.charAt(i);
            }
        } else {                                //如果字符串的长度不够,那么按照对齐方式补齐
            int startIndex;
            switch (alignment) {
                case LEFT:
                    for (int i = 0; i < string.length(); i++) {
                        chars[i] = string.charAt(i);
                    }
                    break;
                case CENTER:
                    startIndex = (width - string.length()) / 2;
                    for (int i = 0; i < string.length(); i++) {
                        chars[i + startIndex] = string.charAt(i);
                    }
                    break;
                case RIGHT:
                    startIndex = width - string.length();
                    for (int i = 0; i < string.length(); i++) {
                        chars[i + startIndex] = string.charAt(i);
                    }
                    break;
            }
        }
        return new String(chars);
    }


}

