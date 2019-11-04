package com.dongzy.common.config;

import com.dongzy.common.common.Validate;
import com.dongzy.common.common.text.StringUtils;

import java.util.Collection;

/**
 * 处理配置项的路径信息
 */
public final class ConfigPathUtils {

    /**
     * 拆分完整路程为code集合
     *
     * @param fullPath 完整路径
     * @return 拆分完成的code集合
     */
    public static Collection<String> splitConfigPath(String fullPath) {

        fullPath = fullPath.trim();
        Collection<String> codes = StringUtils.splitAndRemoveEmpty(fullPath, "\\.");

        for (String code : codes) {
            Validate.isTrue(StringUtils.isEffectiveCode(code), "无效的configPath值：" + fullPath);
        }

        return codes;
    }

    /**
     * 处理路径内容，对路径的可能格式错误进行修正
     *
     * @param fullPath 路径
     * @throws IllegalArgumentException 如果路径为无效路径，将会捕获到此异常
     */
    public static String formatPath(String fullPath) {
        if (fullPath == null) {
            return null;
        }

        Collection<String> codes = splitConfigPath(fullPath);
        return StringUtils.join(".", codes);
    }

    /**
     * 获取当前路径的父路径
     *
     * @param fullPath 当前路径
     * @return 父路径
     */
    public static String getParentPath(String fullPath) {
        Validate.notBlank(fullPath, "fullPath is empty!");

        fullPath = formatPath(fullPath);

        int place = fullPath.lastIndexOf('.');
        return (place != -1) ? fullPath.substring(0, place) : null;
    }

    /**
     * 从路径提取当前路径的code值
     *
     * @param fullPath 当前路径
     * @return 当前的code值
     */
    public static String getCode(String fullPath) {
        Validate.notBlank(fullPath, "fullPath is empty!");

        fullPath = formatPath(fullPath);

        int place = fullPath.lastIndexOf('.');
        return (place != -1) ? fullPath.substring(place + 1) : fullPath;
    }
}
