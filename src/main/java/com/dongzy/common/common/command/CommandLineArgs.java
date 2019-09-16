package com.dongzy.common.common.command;

import com.dongzy.common.common.text.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 命令行参数读取辅助类，用户提取出命令行中的参数信息
 * 本方法可以直接解析从main方法获取到的参数数组，并进行识别操作
 *
 * @author zouyong
 * @since JDK1.6
 */
public final class CommandLineArgs {

    private String[] args;
    private Map<String, String> argumentsMap;

    /**
     * 根据传入的参数构造类
     *
     * @param args 参数数组
     */
    public CommandLineArgs(String[] args) {
        this.args = args;
        argumentsMap = null;
    }

    /**
     * 查询参数的值
     *
     * @param key 查询的参数类型名称
     * @return 参数值
     */
    public String getArgument(String key) {
        if (argumentsMap == null) {
            buildArgs(args);
        }

        key = key.trim().toLowerCase();
        return argumentsMap.getOrDefault(key, StringUtils.EMPTY);
    }

    /**
     * 根据参数数组，生成参数对象集合
     *
     * @param args 参数集合
     * @throws IllegalArgumentException 参数异常
     */
    private void buildArgs(String[] args) throws IllegalArgumentException {

        argumentsMap = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            String keyString = args[i].trim().toLowerCase();
            if (keyString.startsWith("-")) {    //如果该节点是“-”开头，表示为参数类型节点
                String value;
                if (i + 1 > args.length - 1 || args[i + 1].startsWith("-")) {   //如果已经是最后一个节点，或者下一个节点以“-”开头
                    value = StringUtils.EMPTY;
                } else {
                    value = args[i + 1];
                    i++;    //跳到下一个节点
                }
                argumentsMap.put(StringUtils.trim(keyString, '-'), value);
            } else {
                argumentsMap.clear();
                throw new IllegalArgumentException("输入参数数组存在错误，无法被解析！");
            }
        }
    }

}
