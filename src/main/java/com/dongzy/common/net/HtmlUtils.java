package com.dongzy.common.net;

import com.dongzy.common.common.text.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 对HTML内容进行处理的辅助工具
 */
public class HtmlUtils {

    private static final Map<Character, String> CHARACTERS = new HashMap<>();

    static {
        CHARACTERS.put('\"', "&#34;");
        CHARACTERS.put('\\', "&#39;");
        CHARACTERS.put('&', "&#38;");
        CHARACTERS.put('>', "&#62;");
        CHARACTERS.put('<', "&#60;");
        CHARACTERS.put(' ', "&#160;");
    }

    /**
     * 对html内容进行编码
     *
     * @param html 需要编码的内容
     * @return 编码后的内容
     */
    public static String encoder(String html) {
        if (StringUtils.isEmpty(html)) {
            return StringUtils.EMPTY;
        }

        StringBuilder stringBuilder = new StringBuilder(html.length() * 2);
        char[] chars = html.toCharArray();
        for (char c : chars) {
            String s = CHARACTERS.get(c);
            stringBuilder.append(s == null ? c : s);
        }
        return stringBuilder.toString();
    }

    /**
     * 对html内容进行解码
     *
     * @param html 需要解码的内容
     * @return 解码后的内容
     */
    public static String decoder(String html) {
        if (StringUtils.isEmpty(html)) {
            return StringUtils.EMPTY;
        }

        for (Map.Entry<Character, String> entry : CHARACTERS.entrySet()) {
            html = html.replaceAll(entry.getValue(), entry.getKey() + StringUtils.EMPTY);
        }
        return html;
    }

}
