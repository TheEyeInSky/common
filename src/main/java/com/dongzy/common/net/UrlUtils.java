package com.dongzy.common.net;

import com.dongzy.common.common.Validate;
import com.dongzy.common.common.io.PathUtils;
import com.dongzy.common.common.text.StringUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * URL辅助类
 */
public class UrlUtils {

    private final static String AMP_PLACE_HOLDER = "<><><>amp;<><><>";
    private final static String AMP_CHAR = "&amp;";

    /**
     * 采用路径的右斜杠分隔符，将多个字符串连接起来，连接的时候会自动把左斜杠替换成右斜杠，
     * 每一个路径单元之间也会用右斜杠连接起来，
     * 如果被连接的路径中已经包含了左斜杠或者右斜杠，那么系统会自动进行处理，以保证最终连接路径字符串的正确性。
     *
     * @param originalUrl 最初的原URL
     * @param urls        添加被添加的URL
     * @return 连接好的URL
     */
    public static String joinUrl(String originalUrl, String... urls) {
        Validate.notNull(originalUrl);
        if (urls == null) {
            return originalUrl;
        }

        List<String> strPaths = new ArrayList<>(urls.length + 1);
        strPaths.add(StringUtils.trimRight(originalUrl.trim(), PathUtils.SPLITS_URL_PATH).replace(File.separatorChar, '/'));
        for (String url : urls) {
            if (url == null) continue;
            String newPath = StringUtils.trim(url.trim(), PathUtils.SPLITS_URL_PATH).replace(File.separatorChar, '/');
            if (StringUtils.notBlank(newPath))
                strPaths.add(newPath);
        }

        String fullUrl = StringUtils.join("/", strPaths);

        //如果最后一个路径包含文件分隔符，那么就添加一个结尾的路径分隔符
        String lastUrl = urls[urls.length - 1].trim();
        if (lastUrl.endsWith("\\") || lastUrl.endsWith("/")) {
            fullUrl += "/";
        }

        return fullUrl;
    }

    /**
     * 获取采用URL编码字符串
     *
     * @param url url地址
     * @return 编码的完整URL地址
     */
    public static String encoder(String url) {
        if (StringUtils.isEmpty(url)) {
            return StringUtils.EMPTY;
        }
        try {
            return URLEncoder.encode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 获取采用URL解码的字符串
     *
     * @param url 编码过的url地址
     * @return 解码的URL地址
     */
    public static String decoder(String url) {
        if (StringUtils.isEmpty(url)) {
            return StringUtils.EMPTY;
        }
        try {
            return URLDecoder.decode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 给现有的URL地址添加参数
     *
     * @param urlString 原始的URL地址
     * @param parmName  参数的名称
     * @param parmValue 参数的值
     * @return 重构后的URL地址
     */
    public static String addUrlParam(String urlString, String parmName, String parmValue) throws UnsupportedEncodingException {
        Validate.notBlank(urlString, "urlString不能为空!");
        Validate.notBlank(parmName, "url的parmName不能为空!");
        if (StringUtils.isBlank(parmValue)) {
            parmValue = StringUtils.EMPTY;
        }
        urlString = urlString.trim();

        if (urlString.contains("?")) {
            if (urlString.endsWith("?")) {
                urlString = String.format("%s%s=%s", urlString.trim(), parmName.trim(), URLEncoder.encode(parmValue, "utf-8"));
            } else {
                urlString = String.format("%s&%s=%s", urlString.trim(), parmName.trim(), URLEncoder.encode(parmValue, "utf-8"));
            }
        } else {
            urlString = String.format("%s?%s=%s", urlString.trim(), parmName.trim(), URLEncoder.encode(parmValue, "utf-8"));
        }
        return urlString;
    }

    /**
     * 获取URL中的所有查询参数
     *
     * @param urlString url地址
     * @return 查询参数集合
     */
    public static Map<String, String> getParams(final String urlString) {
        Validate.notBlank(urlString, "URL地址不能为空!");

        Map<String, String> params = new HashMap<>();
        int place = urlString.indexOf('?');
        if (place > 0) {
            String paramsString = urlString.substring(place + 1);
            if (paramsString.contains(AMP_CHAR)) {
                paramsString = paramsString.replaceAll(AMP_CHAR, AMP_PLACE_HOLDER);
            }
            String[] paramArray = paramsString.split("&");
            for (String param : paramArray) {
                place = param.indexOf('=');
                String name;
                String value;
                if (place > 0) {
                    name = param.substring(0, place);
                    value = param.substring(place + 1);
                    if (value.contains(AMP_PLACE_HOLDER)) {
                        value = value.replaceAll(AMP_PLACE_HOLDER, AMP_CHAR);
                    }
                    params.put(name, value);
                } else {
                    params.put(param, StringUtils.EMPTY);
                }
            }
        }

        return params;
    }

    /**
     * 获得URL路径,不包含任何的参数
     *
     * @param urlString 原始的URL字符串
     * @return 不含参数的URL字符串
     */
    public static String removeUrlParams(final String urlString) {
        Validate.notBlank(urlString, "URL地址不能为空!");

        int i = urlString.indexOf("?");
        return (i > 0) ? urlString.substring(0, i) : urlString;
    }

    /**
     * 对比两个URL地址是否相等
     *
     * @param url1        地址1
     * @param url2        地址2
     * @param ignoreParam 是否忽略url中的参数
     * @param ignoreCase  是否忽略url字符串的大小写
     * @return 两个地址是否相等
     */
    public static boolean equalUrl(String url1, String url2, boolean ignoreParam, boolean ignoreCase) {
        Validate.notBlank(url1, "URL地址不能为空!");
        Validate.notBlank(url2, "URL地址不能为空!");

        url1 = url1.trim();
        url2 = url2.trim();
        if (ignoreCase) {            //如果忽略大小写,那么都转成小写进行对比
            url1 = url1.toLowerCase();
            url2 = url2.toLowerCase();
        }
        if (ignoreParam) {            //如果忽略参数,那么去掉参数对比
            url1 = removeUrlParams(url1);
            url2 = removeUrlParams(url2);
        }
        boolean same = Objects.equals(url1, url2);

        //如果不相等,要判断是否因为最后是否只相差一个/符号,如果相差/的话也认为两个地址相等
        if (!same && ignoreParam) {
            String newUrl1 = StringUtils.trimRight(removeUrlParams(url1), '/');
            String newUrl2 = StringUtils.trimRight(removeUrlParams(url2), '/');
            same = Objects.equals(newUrl1, newUrl2);
        }

        return same;
    }

    /**
     * 判断一个路径是否是另外一个路径的字路径,
     * 我们会去掉参数来进行路径的对比，
     * /abc     /abc/def        true
     * /abc?do  /abc/def        true
     * /abcd    /abc/def        false
     *
     * @param urlParent  父路径
     * @param urlSub     子路径
     * @param ignoreCase 是否忽略大小写
     * @return 是否为包含关系的路径
     */
    public static boolean isSubUrl(String urlParent, String urlSub, boolean ignoreCase) {
        Validate.notBlank(urlParent, "URL地址不能为空!");
        Validate.notBlank(urlSub, "URL地址不能为空!");

        urlParent = urlParent.trim();
        urlSub = urlSub.trim();
        if (ignoreCase) {            //如果忽略大小写,那么都转成小写进行对比
            urlParent = urlParent.toLowerCase();
            urlSub = urlSub.toLowerCase();
        }
        boolean same = equalUrl(urlParent, urlSub, true, ignoreCase);

        if (!same) {
            urlParent = removeUrlParams(urlParent);
            urlSub = removeUrlParams(urlSub);
            if (!urlParent.endsWith("/")) {
                urlParent += "/";
            }
            same = urlSub.startsWith(urlParent);
        }
        return same;
    }

    /**
     * 尝试从URL中获取包含的文件名称
     *
     * @param url URL地址
     * @return 包含的文件名称
     */
    public static String pickFileName(String url) {
        int splitIndex = url.lastIndexOf("/");
        int index = url.indexOf('?', splitIndex);
        if (index > 0) {
            url = url.substring(0, index);
        }
        return url.substring(splitIndex + 1);
    }
}
