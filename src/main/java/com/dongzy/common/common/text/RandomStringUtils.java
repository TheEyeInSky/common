package com.dongzy.common.common.text;

import com.dongzy.common.common.Validate;

import java.util.Random;
import java.util.UUID;

/**
 * <p>Operations for random {@code String}s.</p>
 * <p>Currently <em>private high surrogate</em> characters are ignored.
 * These are Unicode characters that fall between the values 56192 (db80)
 * and 56319 (dbff) as we don't know how to handle them.
 * High and low surrogates are correctly dealt with - that is if a
 * high surrogate is randomly chosen, 55296 (d800) to 56191 (db7f)
 * then it is followed by a low surrogate. If a low surrogate is chosen,
 * 56320 (dc00) to 57343 (dfff) then it is placed after a randomly
 * chosen high surrogate.</p>
 * <p>RandomStringUtils is intended for simple use cases. For more advanced
 * use cases consider using commons-text
 * <a href="https://commons.apache.org/proper/commons-text/javadocs/api-release/org/apache/commons/text/RandomStringGenerator.html">
 * RandomStringGenerator</a> instead.</p>
 *
 * <p>#ThreadSafe#</p>
 *
 * @since 1.0
 */
public class RandomStringUtils {

    /**
     * 生成随机字符串的可用字符类型
     */
    public enum RandomStringEnum {

        /**
         * 数字
         */
        ONLY_NUMBER,
        /**
         * 字母
         */
        ONLY_LETTER,
        /**
         * 字符加数字
         */
        NUMBER_AND_LETTER

    }

    private static final char[] numbers = new char[]{'2', '3', '4', '5', '6', '7', '8', '9'};
    private static final char[] letters = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    /**
     * 生成随机字符串
     *
     * @param count            生成字符串长度
     * @param randomStringEnum 字符串内容组成
     * @return 字符串
     */
    public static String random(int count, RandomStringEnum randomStringEnum) {
        Validate.isTrue(count >= 0);
        Validate.notNull(randomStringEnum);

        if (count == 0) {
            return StringUtils.EMPTY;
        }

        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            Random random = new Random(UUID.randomUUID().hashCode());

            switch (randomStringEnum) {
                case NUMBER_AND_LETTER:
                    if (i % 2 == 0) {
                        sb.append(numbers[random.nextInt(numbers.length)]);
                    } else {
                        sb.append(letters[random.nextInt(letters.length)]);
                    }
                    break;
                case ONLY_LETTER:
                    sb.append(letters[random.nextInt(letters.length)]);
                    break;
                case ONLY_NUMBER:
                    sb.append(numbers[random.nextInt(numbers.length)]);
                    break;
            }
        }

        return sb.toString();
    }
}
