package com.dongzy.common.common.text;

import com.gee4j.common.BooleanUtils;
import com.gee4j.common.NumberUtils;
import com.gee4j.common.Validate;
import com.gee4j.common.collection.ArrayUtils;
import com.gee4j.common.collection.CollectionUtils;
import com.gee4j.common.time.DateUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

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
public final class StringUtils {

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
     * Represents a failed index search.
     */
    public static final int INDEX_NOT_FOUND = -1;

    //The maximum size to which the padding constant(s) can expand
    private static final int PAD_LIMIT = 8192;

    /**
     * <p>判断字符串是否为空串，同时如果字符串没有任何字符，也会返回true</p>
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     *
     * @param cs 需要判断的字符串
     * @return 字符串是否为空
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * 判断字符串是否为空串
     *
     * @param cs 需要判断的字符串
     * @return 字符串是否不为空
     */
    public static boolean notEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

    /**
     * <p>判断字符串是否为空串，同时如果字符串中的字母全部为空格字符，也会返回true</p>
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param cs 需要判断的字符串
     * @return 字符串是否为空
     */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串不为空串
     *
     * @param cs 需要判断的字符串
     * @return 字符串是否不为空
     */
    public static boolean notBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * 采用指定的连接字符串，将一组字符连接起来，比如用逗号连接一个字符串数组等。
     * 如果字符串数组中只有一个元素，那么将不会添加连接字符串
     * <pre>
     * StringUtils.join(",")                    = ""
     * StringUtils.join(",",[])                 = ""
     * StringUtils.join(",","abc")              = "abc"
     * StringUtils.join(",","hello","world")    = "hello,world"
     * </pre>
     *
     * @param conjunction 用于连接的字符串
     * @param objects     需要连接起来的对象集合
     * @return 连接完成的字符串
     */
    public static String join(final String conjunction, final String... objects) {
        if (ArrayUtils.isEmpty(objects)) {
            return EMPTY;
        } else if (objects.length == 1) {
            return (objects[0] == null) ? StringUtils.EMPTY : objects[0];
        }
        boolean isFirst = true;
        StringBuilder sBuilder = new StringBuilder(50);
        for (Object object : objects) {
            if (isFirst)
                isFirst = false;
            else
                sBuilder.append(conjunction);
            sBuilder.append((object == null) ? StringUtils.EMPTY : object);
        }
        return sBuilder.toString();
    }

    /**
     * 采用指定的连接字符串，将一组字符连接起来，比如用逗号连接一个字符串数组等。
     * 如果字符串数组中只有一个元素，那么将不会添加连接字符串
     *
     * @param conjunction 用于连接的字符串
     * @param objects     需要连接起来的对象集合
     * @return 连接完成的字符串
     */
    public static String join(final String conjunction, final Collection<String> objects) {
        return join(conjunction, CollectionUtils.toArray(objects));
    }

    /**
     * 去掉指定字符串的首尾指定字符，比如去掉字符串首尾的单引号和双引号，那么格式如下：
     * trim(strValue, '\'','\"');
     *
     * @param string    需要处理的字符串
     * @param trimChars 需要移除的字符集合
     * @return 字符串
     */
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

    /**
     * 去掉指定字符串的首尾指定字符，比如去掉字符串首尾的单引号和双引号，那么格式如下：
     * trim(strValue, '\'','\"');
     *
     * @param string    需要处理的字符串
     * @param trimChars 需要移除的字符集合
     * @return 字符串
     */
    public static String trimLeft(final String string, char... trimChars) {

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
    public static String trimRight(final String string, char... trimChars) {

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
     * 将指定字符串根据传入的表达式拆分成字符串集合，并自动移除空项。
     * 同时会对每一个拆出来的字符串做trim操作，去掉空格
     *
     * @param content 需要拆分的内容
     * @param regex   分隔符表达式
     * @return 拆分结果集合
     */
    public static List<String> splitAndRemoveEmpty(final String content, final String regex) {
        Validate.notNull(regex);
        if (content == null) {
            return null;
        }

        if (isBlank(content)) {
            return new ArrayList<>();
        }

        List<String> strCollection = new ArrayList<>();
        String[] strValues = content.split(regex);
        for (String strValue : strValues) {
            if (!isBlank(strValue)) {
                strCollection.add(strValue.trim());
            }
        }
        return strCollection;
    }

    /**
     * 将字符串进行拆分，不移除空白项，并且严格按照拆分符号进行拆分
     * 如果拆分字符在最前面，那么前面将会添加空白项
     * 如果拆分字符出现在最后面，那么最后面将会添加空白项
     *
     * @param content  需要拆分的内容
     * @param splitStr 分隔串
     * @return
     */
    public static List<String> splitNotRemoveEmpty(final String content, final String splitStr) {
        Validate.notEmpty(splitStr, "拆分串不能为空！");
        if (content == null) {
            return null;
        }

        List<String> list = new ArrayList<>();

        int index = 0;
        while (true) {
            int place = content.indexOf(splitStr, index);
            if (place < 0) {
                list.add(content.substring(index));
                break;
            } else if (place == 0) {
                list.add(StringUtils.EMPTY);
                index += splitStr.length();
            } else if (place == content.length() - splitStr.length()) {
                list.add(content.substring(index, place));
                list.add(StringUtils.EMPTY);
                break;
            } else {
                list.add(content.substring(index, place));
                index = place + splitStr.length();
            }
        }

        return list;
    }

    /**
     * 替换文本中的所有匹配字符串，
     * 不采用正则表达式
     * 会全部替换匹配的结果
     * <pre>
     * StringUtils.replace(null, *, *)        = null
     * StringUtils.replace("", *, *)          = ""
     * StringUtils.replace("any", null, *)    = "any"
     * StringUtils.replace("any", *, null)    = "any"
     * StringUtils.replace("any", "", *)      = "any"
     * StringUtils.replace("aba", "a", null)  = "aba"
     * StringUtils.replace("aba", "a", "")    = "b"
     * StringUtils.replace("aba", "a", "z")   = "zbz"
     * </pre>
     *
     * @param text         被分析处理的原始字符串
     * @param searchString 需要查找的字符串
     * @param replacement  被替换字符串
     * @return 替换完成的结果
     */
    public static String replace(final String text, final String searchString, final String replacement) {
        return replace(text, searchString, replacement, -1, false);
    }

    /**
     * <p>Case insensitively replaces all occurrences of a String within another String.</p>
     * <p>
     * <p>A {@code null} reference passed to this method is a no-op.</p>
     *
     * <pre>
     * StringUtils.replaceIgnoreCase(null, *, *)        = null
     * StringUtils.replaceIgnoreCase("", *, *)          = ""
     * StringUtils.replaceIgnoreCase("any", null, *)    = "any"
     * StringUtils.replaceIgnoreCase("any", *, null)    = "any"
     * StringUtils.replaceIgnoreCase("any", "", *)      = "any"
     * StringUtils.replaceIgnoreCase("aba", "a", null)  = "aba"
     * StringUtils.replaceIgnoreCase("abA", "A", "")    = "b"
     * StringUtils.replaceIgnoreCase("aba", "A", "z")   = "zbz"
     * </pre>
     *
     * @param text         text to search and replace in, may be null
     * @param searchString the String to search for (case insensitive), may be null
     * @param replacement  the String to replace it with, may be null
     * @return the text with any replacements processed,
     * {@code null} if null String input
     * @since 3.5
     */
    public static String replaceIgnoreCase(final String text, final String searchString, final String replacement) {
        return replace(text, searchString, replacement, -1, true);
    }

    private static String replace(final String text, String searchString, final String replacement, int max, final boolean ignoreCase) {
        if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
            return text;
        }
        String searchText = text;
        if (ignoreCase) {
            searchText = text.toLowerCase();
            searchString = searchString.toLowerCase();
        }
        int start = 0;
        int end = searchText.indexOf(searchString, start);
        if (end == INDEX_NOT_FOUND) {
            return text;
        }
        final int replLength = searchString.length();
        int increase = replacement.length() - replLength;
        increase = increase < 0 ? 0 : increase;
        increase *= max < 0 ? 16 : max > 64 ? 64 : max;
        final StringBuilder sb = new StringBuilder(text.length() + increase);
        while (end != INDEX_NOT_FOUND) {
            sb.append(text, start, end).append(replacement);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = searchText.indexOf(searchString, start);
        }
        sb.append(text, start, text.length());
        return sb.toString();
    }

    /**
     * <p>Gets {@code len} characters from the middle of a String.</p>
     * <p>
     * <p>If {@code len} characters are not available, the remainder
     * of the String will be returned without an exception. If the
     * String is {@code null}, {@code null} will be returned.
     * An empty String is returned if len is negative or exceeds the
     * length of {@code str}.</p>
     *
     * <pre>
     * StringUtils.mid(null, *, *)    = null
     * StringUtils.mid(*, *, -ve)     = ""
     * StringUtils.mid("", 0, *)      = ""
     * StringUtils.mid("abc", 0, 2)   = "ab"
     * StringUtils.mid("abc", 0, 4)   = "abc"
     * StringUtils.mid("abc", 2, 4)   = "c"
     * StringUtils.mid("abc", 4, 2)   = ""
     * StringUtils.mid("abc", -2, 2)  = "ab"
     * </pre>
     *
     * @param str the String to get the characters from, may be null
     * @param pos the position to start from, negative treated as zero
     * @param len the length of the required String
     * @return the middle characters, {@code null} if null String input
     */
    public static String mid(final String str, int pos, final int len) {
        if (str == null) {
            return null;
        }
        if (len < 0 || pos > str.length()) {
            return EMPTY;
        }
        if (pos < 0) {
            pos = 0;
        }
        if (str.length() <= pos + len) {
            return str.substring(pos);
        }
        return str.substring(pos, pos + len);
    }

    /**
     * <p>Compares two CharSequences, returning {@code true} if they represent
     * equal sequences of characters, ignoring case.</p>
     * <p>
     * <p>{@code null}s are handled without exceptions. Two {@code null}
     * references are considered equal. Comparison is case insensitive.</p>
     *
     * <pre>
     * StringUtils.equalsIgnoreCase(null, null)   = true
     * StringUtils.equalsIgnoreCase(null, "abc")  = false
     * StringUtils.equalsIgnoreCase("abc", null)  = false
     * StringUtils.equalsIgnoreCase("abc", "abc") = true
     * StringUtils.equalsIgnoreCase("abc", "ABC") = true
     * </pre>
     *
     * @param str1 the first CharSequence, may be null
     * @param str2 the second CharSequence, may be null
     * @return {@code true} if the CharSequence are equal, case insensitive, or
     * both {@code null}
     * @since 3.0 Changed signature from equalsIgnoreCase(String, String) to equalsIgnoreCase(CharSequence, CharSequence)
     */
    public static boolean equalsIgnoreCase(final CharSequence str1, final CharSequence str2) {
        if (str1 == null || str2 == null) {
            return str1 == str2;
        } else if (str1 == str2) {
            return true;
        } else if (str1.length() != str2.length()) {
            return false;
        } else {
            return CharSequenceUtils.regionMatches(str1, true, 0, str2, 0, str1.length());
        }
    }

    /**
     * <p>Compare two Strings lexicographically, ignoring case differences,
     * as per {@link String#compareToIgnoreCase(String)}, returning :</p>
     * <ul>
     * <li>{@code int = 0}, if {@code str1} is equal to {@code str2} (or both {@code null})</li>
     * <li>{@code int < 0}, if {@code str1} is less than {@code str2}</li>
     * <li>{@code int > 0}, if {@code str1} is greater than {@code str2}</li>
     * </ul>
     * <p>
     * <p>This is a {@code null} safe version of :</p>
     * <blockquote><pre>str1.compareToIgnoreCase(str2)</pre></blockquote>
     * <p>
     * <p>{@code null} inputs are handled according to the {@code nullIsLess} parameter.
     * Two {@code null} references are considered equal.
     * Comparison is case insensitive.</p>
     *
     * <pre>
     * StringUtils.compareIgnoreCase(null, null)   = 0
     * StringUtils.compareIgnoreCase(null , "a")   &lt; 0
     * StringUtils.compareIgnoreCase("a", null)    &gt; 0
     * StringUtils.compareIgnoreCase("abc", "abc") = 0
     * StringUtils.compareIgnoreCase("abc", "ABC") = 0
     * StringUtils.compareIgnoreCase("a", "b")     &lt; 0
     * StringUtils.compareIgnoreCase("b", "a")     &gt; 0
     * StringUtils.compareIgnoreCase("a", "B")     &lt; 0
     * StringUtils.compareIgnoreCase("A", "b")     &lt; 0
     * StringUtils.compareIgnoreCase("ab", "ABC")  &lt; 0
     * </pre>
     *
     * @param str1 the String to compare from
     * @param str2 the String to compare to
     * @return &lt; 0, 0, &gt; 0, if {@code str1} is respectively less, equal ou greater than {@code str2},
     * ignoring case differences.
     * @see String#compareToIgnoreCase(String)
     * @since 3.5
     */
    public static int compareIgnoreCase(final String str1, final String str2) {
        if (Objects.equals(str1, str2)) {
            return 0;
        }
        if (str1 == null) {
            return -1;
        }
        if (str2 == null) {
            return 1;
        }
        return str1.compareToIgnoreCase(str2);
    }

    /**
     * <p>Case in-sensitive find of the first index within a CharSequence.</p>
     * <p>
     * <p>A {@code null} CharSequence will return {@code -1}.
     * A negative start position is treated as zero.
     * An empty ("") search CharSequence always matches.
     * A start position greater than the string length only matches
     * an empty search CharSequence.</p>
     *
     * <pre>
     * StringUtils.indexOfIgnoreCase(null, *)          = -1
     * StringUtils.indexOfIgnoreCase(*, null)          = -1
     * StringUtils.indexOfIgnoreCase("", "")           = 0
     * StringUtils.indexOfIgnoreCase("aabaabaa", "a")  = 0
     * StringUtils.indexOfIgnoreCase("aabaabaa", "b")  = 2
     * StringUtils.indexOfIgnoreCase("aabaabaa", "ab") = 1
     * </pre>
     *
     * @param str       the CharSequence to check, may be null
     * @param searchStr the CharSequence to find, may be null
     * @return the first index of the search CharSequence,
     * -1 if no match or {@code null} string input
     * @since 3.0 Changed signature from indexOfIgnoreCase(String, String) to indexOfIgnoreCase(CharSequence, CharSequence)
     */
    public static int indexOfIgnoreCase(final CharSequence str, final CharSequence searchStr) {
        return indexOfIgnoreCase(str, searchStr, 0);
    }

    /**
     * <p>Case in-sensitive find of the first index within a CharSequence
     * from the specified position.</p>
     * <p>
     * <p>A {@code null} CharSequence will return {@code -1}.
     * A negative start position is treated as zero.
     * An empty ("") search CharSequence always matches.
     * A start position greater than the string length only matches
     * an empty search CharSequence.</p>
     *
     * <pre>
     * StringUtils.indexOfIgnoreCase(null, *, *)          = -1
     * StringUtils.indexOfIgnoreCase(*, null, *)          = -1
     * StringUtils.indexOfIgnoreCase("", "", 0)           = 0
     * StringUtils.indexOfIgnoreCase("aabaabaa", "A", 0)  = 0
     * StringUtils.indexOfIgnoreCase("aabaabaa", "B", 0)  = 2
     * StringUtils.indexOfIgnoreCase("aabaabaa", "AB", 0) = 1
     * StringUtils.indexOfIgnoreCase("aabaabaa", "B", 3)  = 5
     * StringUtils.indexOfIgnoreCase("aabaabaa", "B", 9)  = -1
     * StringUtils.indexOfIgnoreCase("aabaabaa", "B", -1) = 2
     * StringUtils.indexOfIgnoreCase("aabaabaa", "", 2)   = 2
     * StringUtils.indexOfIgnoreCase("abc", "", 9)        = -1
     * </pre>
     *
     * @param str       the CharSequence to check, may be null
     * @param searchStr the CharSequence to find, may be null
     * @param startPos  the start position, negative treated as zero
     * @return the first index of the search CharSequence (always &ge; startPos),
     * -1 if no match or {@code null} string input
     * @since 3.0 Changed signature from indexOfIgnoreCase(String, String, int) to indexOfIgnoreCase(CharSequence, CharSequence, int)
     */
    public static int indexOfIgnoreCase(final CharSequence str, final CharSequence searchStr, int startPos) {
        if (str == null || searchStr == null) {
            return INDEX_NOT_FOUND;
        }
        if (startPos < 0) {
            startPos = 0;
        }
        final int endLimit = str.length() - searchStr.length() + 1;
        if (startPos > endLimit) {
            return INDEX_NOT_FOUND;
        }
        if (searchStr.length() == 0) {
            return startPos;
        }
        for (int i = startPos; i < endLimit; i++) {
            if (CharSequenceUtils.regionMatches(str, true, i, searchStr, 0, searchStr.length())) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Case in-sensitive find of the last index within a CharSequence.</p>
     * <p>
     * <p>A {@code null} CharSequence will return {@code -1}.
     * A negative start position returns {@code -1}.
     * An empty ("") search CharSequence always matches unless the start position is negative.
     * A start position greater than the string length searches the whole string.</p>
     *
     * <pre>
     * StringUtils.lastIndexOfIgnoreCase(null, *)          = -1
     * StringUtils.lastIndexOfIgnoreCase(*, null)          = -1
     * StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A")  = 7
     * StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B")  = 5
     * StringUtils.lastIndexOfIgnoreCase("aabaabaa", "AB") = 4
     * </pre>
     *
     * @param str       the CharSequence to check, may be null
     * @param searchStr the CharSequence to find, may be null
     * @return the first index of the search CharSequence,
     * -1 if no match or {@code null} string input
     * @since 3.0 Changed signature from lastIndexOfIgnoreCase(String, String) to lastIndexOfIgnoreCase(CharSequence, CharSequence)
     */
    public static int lastIndexOfIgnoreCase(final CharSequence str, final CharSequence searchStr) {
        if (str == null || searchStr == null) {
            return INDEX_NOT_FOUND;
        }
        return lastIndexOfIgnoreCase(str, searchStr, str.length());
    }

    /**
     * <p>Case in-sensitive find of the last index within a CharSequence
     * from the specified position.</p>
     * <p>
     * <p>A {@code null} CharSequence will return {@code -1}.
     * A negative start position returns {@code -1}.
     * An empty ("") search CharSequence always matches unless the start position is negative.
     * A start position greater than the string length searches the whole string.
     * The search starts at the startPos and works backwards; matches starting after the start
     * position are ignored.
     * </p>
     *
     * <pre>
     * StringUtils.lastIndexOfIgnoreCase(null, *, *)          = -1
     * StringUtils.lastIndexOfIgnoreCase(*, null, *)          = -1
     * StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A", 8)  = 7
     * StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 8)  = 5
     * StringUtils.lastIndexOfIgnoreCase("aabaabaa", "AB", 8) = 4
     * StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 9)  = 5
     * StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", -1) = -1
     * StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A", 0)  = 0
     * StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 0)  = -1
     * </pre>
     *
     * @param str       the CharSequence to check, may be null
     * @param searchStr the CharSequence to find, may be null
     * @param startPos  the start position
     * @return the last index of the search CharSequence (always &le; startPos),
     * -1 if no match or {@code null} input
     * @since 3.0 Changed signature from lastIndexOfIgnoreCase(String, String, int) to lastIndexOfIgnoreCase(CharSequence, CharSequence, int)
     */
    public static int lastIndexOfIgnoreCase(final CharSequence str, final CharSequence searchStr, int startPos) {
        if (str == null || searchStr == null) {
            return INDEX_NOT_FOUND;
        }
        if (startPos > str.length() - searchStr.length()) {
            startPos = str.length() - searchStr.length();
        }
        if (startPos < 0) {
            return INDEX_NOT_FOUND;
        }
        if (searchStr.length() == 0) {
            return startPos;
        }

        for (int i = startPos; i >= 0; i--) {
            if (CharSequenceUtils.regionMatches(str, true, i, searchStr, 0, searchStr.length())) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Checks if CharSequence contains a search CharSequence irrespective of case,
     * handling {@code null}. Case-insensitivity is defined as by
     * {@link String#equalsIgnoreCase(String)}.
     * <p>
     * <p>A {@code null} CharSequence will return {@code false}.</p>
     *
     * <pre>
     * StringUtils.containsIgnoreCase(null, *) = false
     * StringUtils.containsIgnoreCase(*, null) = false
     * StringUtils.containsIgnoreCase("", "") = true
     * StringUtils.containsIgnoreCase("abc", "") = true
     * StringUtils.containsIgnoreCase("abc", "a") = true
     * StringUtils.containsIgnoreCase("abc", "z") = false
     * StringUtils.containsIgnoreCase("abc", "A") = true
     * StringUtils.containsIgnoreCase("abc", "Z") = false
     * </pre>
     *
     * @param str       the CharSequence to check, may be null
     * @param searchStr the CharSequence to find, may be null
     * @return true if the CharSequence contains the search CharSequence irrespective of
     * case or false if not or {@code null} string input
     * @since 3.0 Changed signature from containsIgnoreCase(String, String) to containsIgnoreCase(CharSequence, CharSequence)
     */
    public static boolean containsIgnoreCase(final CharSequence str, final CharSequence searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        final int len = searchStr.length();
        final int max = str.length() - len;
        for (int i = 0; i <= max; i++) {
            if (CharSequenceUtils.regionMatches(str, true, i, searchStr, 0, len)) {
                return true;
            }
        }
        return false;
    }

    // Left/Right/Mid
    //-----------------------------------------------------------------------

    /**
     * <p>Gets the leftmost {@code len} characters of a String.</p>
     * <p>
     * <p>If {@code len} characters are not available, or the
     * String is {@code null}, the String will be returned without
     * an exception. An empty String is returned if len is negative.</p>
     *
     * <pre>
     * StringUtils.left(null, *)    = null
     * StringUtils.left(*, -ve)     = ""
     * StringUtils.left("", *)      = ""
     * StringUtils.left("abc", 0)   = ""
     * StringUtils.left("abc", 2)   = "ab"
     * StringUtils.left("abc", 4)   = "abc"
     * </pre>
     *
     * @param str the String to get the leftmost characters from, may be null
     * @param len the length of the required String
     * @return the leftmost characters, {@code null} if null String input
     */
    public static String left(final String str, final int len) {
        if (str == null) {
            return null;
        }
        if (len < 0) {
            return EMPTY;
        }
        if (str.length() <= len) {
            return str;
        }
        return str.substring(0, len);
    }

    /**
     * <p>Gets the rightmost {@code len} characters of a String.</p>
     * <p>
     * <p>If {@code len} characters are not available, or the String
     * is {@code null}, the String will be returned without an
     * an exception. An empty String is returned if len is negative.</p>
     *
     * <pre>
     * StringUtils.right(null, *)    = null
     * StringUtils.right(*, -ve)     = ""
     * StringUtils.right("", *)      = ""
     * StringUtils.right("abc", 0)   = ""
     * StringUtils.right("abc", 2)   = "bc"
     * StringUtils.right("abc", 4)   = "abc"
     * </pre>
     *
     * @param str the String to get the rightmost characters from, may be null
     * @param len the length of the required String
     * @return the rightmost characters, {@code null} if null String input
     */
    public static String right(final String str, final int len) {
        if (str == null) {
            return null;
        }
        if (len < 0) {
            return EMPTY;
        }
        if (str.length() <= len) {
            return str;
        }
        return str.substring(str.length() - len);
    }

    // SubStringAfter/SubStringBefore
    //-----------------------------------------------------------------------

    /**
     * <p>Gets the substring before the first occurrence of a separator.
     * The separator is not returned.</p>
     * <p>
     * <p>A {@code null} string input will return {@code null}.
     * An empty ("") string input will return the empty string.
     * A {@code null} separator will return the input string.</p>
     * <p>
     * <p>If nothing is found, the string input is returned.</p>
     *
     * <pre>
     * StringUtils.substringBefore(null, *)      = null
     * StringUtils.substringBefore("", *)        = ""
     * StringUtils.substringBefore("abc", "a")   = ""
     * StringUtils.substringBefore("abcba", "b") = "a"
     * StringUtils.substringBefore("abc", "c")   = "ab"
     * StringUtils.substringBefore("abc", "d")   = "abc"
     * StringUtils.substringBefore("abc", "")    = ""
     * StringUtils.substringBefore("abc", null)  = "abc"
     * </pre>
     *
     * @param str       the String to get a substring from, may be null
     * @param separator the String to search for, may be null
     * @return the substring before the first occurrence of the separator,
     * {@code null} if null String input
     * @since 2.0
     */
    public static String substringBefore(final String str, final String separator) {
        if (isEmpty(str) || separator == null) {
            return str;
        }
        if (separator.isEmpty()) {
            return EMPTY;
        }
        final int pos = str.indexOf(separator);
        if (pos == INDEX_NOT_FOUND) {
            return str;
        }
        return str.substring(0, pos);
    }

    /**
     * <p>Gets the substring after the first occurrence of a separator.
     * The separator is not returned.</p>
     * <p>
     * <p>A {@code null} string input will return {@code null}.
     * An empty ("") string input will return the empty string.
     * A {@code null} separator will return the empty string if the
     * input string is not {@code null}.</p>
     * <p>
     * <p>If nothing is found, the empty string is returned.</p>
     *
     * <pre>
     * StringUtils.substringAfter(null, *)      = null
     * StringUtils.substringAfter("", *)        = ""
     * StringUtils.substringAfter(*, null)      = ""
     * StringUtils.substringAfter("abc", "a")   = "bc"
     * StringUtils.substringAfter("abcba", "b") = "cba"
     * StringUtils.substringAfter("abc", "c")   = ""
     * StringUtils.substringAfter("abc", "d")   = ""
     * StringUtils.substringAfter("abc", "")    = "abc"
     * </pre>
     *
     * @param str       the String to get a substring from, may be null
     * @param separator the String to search for, may be null
     * @return the substring after the first occurrence of the separator,
     * {@code null} if null String input
     * @since 2.0
     */
    public static String substringAfter(final String str, final String separator) {
        if (isEmpty(str)) {
            return str;
        }
        if (separator == null) {
            return EMPTY;
        }
        final int pos = str.indexOf(separator);
        if (pos == INDEX_NOT_FOUND) {
            return EMPTY;
        }
        return str.substring(pos + separator.length());
    }

    /**
     * <p>Returns padding using the specified delimiter repeated
     * to a given length.</p>
     *
     * <pre>
     * StringUtils.repeat('e', 0)  = ""
     * StringUtils.repeat('e', 3)  = "eee"
     * StringUtils.repeat('e', -2) = ""
     * </pre>
     *
     * <p>Note: this method does not support padding with
     * <a href="http://www.unicode.org/glossary/#supplementary_character">Unicode Supplementary Characters</a>
     * as they require a pair of {@code char}s to be represented.
     * If you are needing to support full I18N of your applications
     * consider using {@link #repeat(String, int)} instead.
     * </p>
     *
     * @param ch     character to repeat
     * @param repeat number of times to repeat char, negative treated as zero
     * @return String with repeated character
     * @see #repeat(String, int)
     */
    public static String repeat(final char ch, final int repeat) {
        if (repeat <= 0) {
            return EMPTY;
        }
        final char[] buf = new char[repeat];
        for (int i = repeat - 1; i >= 0; i--) {
            buf[i] = ch;
        }
        return new String(buf);
    }

    // Padding
    //-----------------------------------------------------------------------

    /**
     * <p>Repeat a String {@code repeat} times to form a
     * new String.</p>
     *
     * <pre>
     * StringUtils.repeat(null, 2) = null
     * StringUtils.repeat("", 0)   = ""
     * StringUtils.repeat("", 2)   = ""
     * StringUtils.repeat("a", 3)  = "aaa"
     * StringUtils.repeat("ab", 2) = "abab"
     * StringUtils.repeat("a", -2) = ""
     * </pre>
     *
     * @param str    the String to repeat, may be null
     * @param repeat number of times to repeat str, negative treated as zero
     * @return a new String consisting of the original String repeated,
     * {@code null} if null String input
     */
    public static String repeat(final String str, final int repeat) {
        if (str == null) {
            return null;
        }
        if (repeat <= 0) {
            return EMPTY;
        }
        final int inputLength = str.length();
        if (repeat == 1 || inputLength == 0) {
            return str;
        }
        if (inputLength == 1 && repeat <= PAD_LIMIT) {
            return repeat(str.charAt(0), repeat);
        }

        final int outputLength = inputLength * repeat;
        switch (inputLength) {
            case 1:
                return repeat(str.charAt(0), repeat);
            case 2:
                final char ch0 = str.charAt(0);
                final char ch1 = str.charAt(1);
                final char[] output2 = new char[outputLength];
                for (int i = repeat * 2 - 2; i >= 0; i--, i--) {
                    output2[i] = ch0;
                    output2[i + 1] = ch1;
                }
                return new String(output2);
            default:
                final StringBuilder buf = new StringBuilder(outputLength);
                for (int i = 0; i < repeat; i++) {
                    buf.append(str);
                }
                return buf.toString();
        }
    }

    /**
     * 将字符串转换成二进制数组
     *
     * @param str 需要转换的字符串
     * @return 二进制数组
     */
    public static byte[] getBytes(String str) {
        if (str == null) {
            return null;
        }
        return str.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 判断字符内容是否为中文字符
     *
     * @param c 需要识别的字符
     * @return 是否为中文
     */
    public static boolean isChineseChar(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    /**
     * 判断字符内容是否为中文字符
     *
     * @param c 需要识别的字符
     * @return 是否为中文
     */
    public static boolean isEnglishChar(char c) {
        return c <= 0x00FF; //英文字符
    }

    //半角标点符合
    private static final char[] halfAngle = "~!@#$%^&*()_+-={}|[]\\:\";'<>?,./".toCharArray();
    //全角标点符合
    private static final char[] wholeAngle = "～！＠＃￥％︿＆×（）＿＋－＝｛｝｜【】＼：＂；＇《》？，．／".toCharArray();

    /**
     * 将字符串的英文标点符合替换成中文
     *
     * @param str 需要替换的字符串
     * @return 替换后的结果
     */
    public static String replacePunctuation(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            for (int j = 0; j < halfAngle.length; j++) {
                if (chars[i] == halfAngle[j]) {
                    chars[i] = wholeAngle[j];
                    break;
                }
            }
        }
        return new String(chars);
    }

    /**
     * 检查字符串是否为java合法的变量名字符串
     *
     * @param identifier 变量名
     * @return 是否合法
     */
    public static boolean isJavaIdentifier(final String identifier) {
        if (StringUtils.isBlank(identifier)) {
            return false;
        }

        char firstChar = identifier.charAt(0);
        boolean isOk = false;
        if ((firstChar >= 'a' && firstChar <= 'z') || (firstChar >= 'A' && firstChar <= 'Z') || firstChar == '_') {
            isOk = true;
            for (int i = 1; i < identifier.length(); i++) {
                if (!Character.isJavaIdentifierPart(identifier.charAt(i))) {
                    isOk = false;
                    break;
                }
            }
        }

        return isOk;
    }

    private static final Pattern CODE_PATTERN = Pattern.compile("^[\\w-]+$");

    /**
     * 检查字符串是否为一个有效的code字符串
     *
     * @param code 需要检查的code
     * @return 是否合法
     */
    public static boolean isEffectiveCode(final String code) {
        if (StringUtils.isBlank(code)) {
            return false;
        }
        return CODE_PATTERN.matcher(code).matches();
    }

    /**
     * 将对象转换为文本字符串
     *
     * @param value 需要转换的对象
     * @return 文本内容
     */
    public static String toString(Object value) {
        if (value == null) {
            return EMPTY;
        }

        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Number) {
            return NumberUtils.toString((Number) value);
        } else if (value instanceof Date) {
            return DateUtils.toShortString((Date) value);
        } else if (value instanceof LocalDate || value instanceof LocalDateTime) {
            return DateUtils.toShortString(DateUtils.tryToDate(value));
        } else if (value instanceof Boolean) {
            return BooleanUtils.toString((Boolean) value);
        } else {
            return value.toString();
        }
    }

    /**
     * 将普通文本转换为unicode存储
     *
     * @param input 输入的字符串
     * @return unicode文本
     */
    public static String stringToUnicode(final String input) {
        if (input == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (ch <= 255) {
                stringBuilder.append(ch);
            } else {
                stringBuilder.append("\\u").append(Integer.toHexString(ch));
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 将unicode转换为普通文本
     *
     * @param input 输入unicode串
     * @return 普通文本
     */
    public static String unicodeToString(final String input) {
        if (input == null) {
            return null;
        }
        //如果不是unicode码则原样返回
        if (!input.contains("\\u")) {
            return input;
        }

        StringBuilder sb = new StringBuilder(1000);
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (ch == '\\' && (i + 1 < input.length()) && input.charAt(i + 1) == 'u') {
                try {
                    char ch1 = (char) Integer.parseInt(input.substring(i + 2, i + 6), 16);
                    sb.append(ch1);
                    i += 5;
                } catch (Throwable t) {
                    sb.append(ch);
                }
            } else {
                sb.append(ch);
            }
        }

        return sb.toString();
    }
}
