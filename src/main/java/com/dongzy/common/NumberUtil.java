package com.dongzy.common;

import java.math.BigDecimal;

/**
 * @author dongzy
 * @Desc
 * @date 2019/9/16.
 */

public class NumberUtil {
    public static final String EMPTY = "";
    public static final char[] TRIM_DEFAULT_CHARS = new char[]{' ', '\n', '\r', '\t'};
    /**
     * 保留有效数字
     * @param number
     * @return
     */
    public static String toString(Number number) {
        if (number == null) {
            return null;
        } else {
            String value = (new BigDecimal(Double.toString(number.doubleValue()))).toPlainString();
            if (!value.contains(".")) {
                return value;
            } else {
                int length = value.length() - 1;

                for(int i = length; i >= 0; --i) {
                    char c = value.charAt(i);
                    if (c != '0') {
                        length = i;
                        break;
                    }
                }

                value = value.substring(0, length + 1);
                return trim(value, '.');
            }
        }
    }
    public static String trim(final String string, char... trimChars) {

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
}
