package com.dongzy.common.common.text;

import com.gee4j.common.collection.ArrayUtils;
import com.gee4j.common.collection.CollectionUtils;
import com.gee4j.common.collection.ExhaustiveArrays;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 拼音的辅助类,用于返回字符串的全拼或者简拼(每个字的首字母)
 * <p>
 * 对于返回拼音集合的方法,返回集合的数量于字符串中多音字的个数有关系
 * 如果没有多音字,那么数量为一,如果有多音字,那么数量为多音字数量的乘积.
 * 比如:如果一个字符串中有两个多音字,这两个字分别有两个和三个不同的拼音,
 * 那么返回的集合数量就是2*3=6个
 * </p>
 *
 * @author 邹勇
 * @version JDK 6.0
 */
public final class Pinyin {

    private static HanyuPinyinOutputFormat format;

    static {
        format = new HanyuPinyinOutputFormat();
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    /**
     * 返回字符串的拼音集合,返回集合的数量于字符串中存在多音字的多少有密切关系
     * 如果字符串中没有多音字,那么返回的数量为一,如果存在多个多音字,那么集合数量就是它们的乘积.
     *
     * @param str 获取拼音的字符串
     * @return 拼音字符串数组
     */
    public static Collection<String> fullPinyins(String str) {
        if (StringUtils.isBlank(str)) {
            return new ArrayList<>(0);
        }

        List<String[]> collections = new ArrayList<>();
        for (int i = 0; i < str.length(); ++i) {
            String[] strings = characterPinYins(str.charAt(i));
            if (strings != null && strings.length > 0) {
                collections.add(strings);
            }
        }

        return exhaustive(collections);
    }

    /**
     * 返回字符串的拼音数组,每个汉字对应一个拼音字符串
     *
     * @param str 获取拼音的字符串
     * @return 拼音字符串数组
     */
    public static Collection<String> jianPinyins(String str) {
        if (StringUtils.isBlank(str)) {
            return new ArrayList<>(0);
        }

        List<String[]> collections = new ArrayList<>();
        for (int i = 0; i < str.length(); ++i) {
            String[] strings = characterPinYins(str.charAt(i));
            if (strings == null || strings.length == 0) {
                continue;
            }

            //获取首字母,并去重
            Collection<Character> characters = new ArrayList<>();
            for (String string : strings) {
                if (!characters.contains(string.charAt(0))) {
                    characters.add(string.charAt(0));
                }
            }
            collections.add(CollectionUtils.toArray(
                    characters.stream().map(String::valueOf).collect(Collectors.toList())));
        }

        return exhaustive(collections);
    }

    //将拼音数组穷举,并拼串输出
    private static Collection<String> exhaustive(List<String[]> collections) {
        String[][] pinyinStrings = new String[collections.size()][];
        for (int i = 0; i < collections.size(); i++) {
            pinyinStrings[i] = CollectionUtils.toArray(
                    ArrayUtils.toCollection(collections.get(i)).stream().distinct().collect(Collectors.toList()));
        }

        List<List<String>> pinyinList = ExhaustiveArrays.exhaustive(pinyinStrings);

        Collection<String> result = new ArrayList<>();
        for (List<String> list : pinyinList) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String string : list) {
                stringBuilder.append(string);
            }
            result.add(stringBuilder.toString());
        }

        return result;
    }

    /**
     * 返回字符串的全拼
     *
     * @param str 需要返回拼音的字符串
     * @return 字符串的全拼
     */
    public static String fullPinyin(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); ++i) {
            String tempPinyin = characterPinYin(str.charAt(i));
            if (tempPinyin != null) {    // 如果str.charAt(i)非汉字，直接忽略掉
                sb.append(tempPinyin);
            }
        }
        return sb.toString();
    }

    /**
     * 返回字符串每次字的首字母拼音
     *
     * @param str 需要返回拼音的字符串
     * @return 字符串的简拼
     */
    public static String jianPinyin(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); ++i) {
            String tempPinyin = characterPinYin(str.charAt(i));
            if (tempPinyin != null) {    // 如果str.charAt(i)非汉字，直接忽略掉
                sb.append(tempPinyin.charAt(0));
            }
        }
        return sb.toString();
    }

    /**
     * 转换单个字符
     *
     * @param c 单个字符
     * @return 字符的完整拼音
     */
    public static String characterPinYin(Character c) {
        if (c == null) {
            return null;
        }

        try {
            // 如果c不是汉字，toHanyuPinyinStringArray会返回null
            String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(c, format);
            if (pinyin != null && pinyin.length > 0)        // 只取一个发音，如果是多音字，仅取第一个发音
                return pinyin[0];
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 转换单个字符的所有拼音,如果为多音字,那么将会有多个拼音
     *
     * @param c 单个字符
     * @return 字符的完整拼音的集合
     */
    public static String[] characterPinYins(Character c) {
        if (c == null) {
            return null;
        }

        try {
            // 如果c不是汉字，toHanyuPinyinStringArray会返回null
            return PinyinHelper.toHanyuPinyinStringArray(c, format);
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }

        return null;
    }

}
