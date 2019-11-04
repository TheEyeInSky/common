package com.dongzy.common.common.text;

import com.dongzy.common.data.table.DataRow;
import com.dongzy.common.data.table.DataTable;
import com.dongzy.common.common.NumberUtils;
import com.dongzy.common.common.Validate;
import com.dongzy.common.common.time.DateUtils;
import com.dongzy.common.data.table.DataRow;
import com.dongzy.common.data.table.DataTable;

import java.util.Date;

/**
 * 本工具主要用于将需要展示的文本数据进行格式化输出
 * 比如对表格数据进行格式化输入、输入固定宽度的文本等等
 */
public class FormatData {

    /**
     * null文本格式化后的内容
     */
    private static final String NULL_STRING = "NULL";

    /**
     * 获取固定宽度的字符串，如果字符串宽度过长，会被截断，如果字符串宽度不够，会居中显示
     *
     * @param message 需要显示的内容
     * @param width   内容所占的宽度
     * @return 字符串
     */
    public static String getStringByFixWidth(final String message, int width) {
        return getStringByFixWidth(message, width, HorizontalAlignmentEnum.CENTER);
    }

    /**
     * 获取固定宽度的字符串，如果字符串宽度过长，会被截断，如果字符串宽度不够,会按照指定的对齐方式对齐
     *
     * @param message   需要显示的内容
     * @param width     内容所占的宽度
     * @param alignment 水平对齐方式
     * @return 字符串
     */
    public static String getStringByFixWidth(final String message, int width, HorizontalAlignmentEnum alignment) {
        String string = (message == null) ? NULL_STRING : message;

        char[] chars = new char[width];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = ' ';
        }

        //截取出来能够被展示的内容长度
        int contentWidth = 0;
        for (int i = 0; i < string.length(); i++) {
            contentWidth += getShowWidth(string.charAt(i));
            if (contentWidth <= width) {
                chars[i] = string.charAt(i);
            } else {
                break;
            }
        }

        string = new String(chars).trim();
        //获取内容展示的宽度
        contentWidth = Math.min(getShowWidth(string), width);

        switch (alignment) {
            case LEFT:
                return string + StringUtils.repeat(' ', width - contentWidth);
            case CENTER:
                int startIndex = (width - contentWidth) / 2;
                return StringUtils.repeat(' ', startIndex) + string + StringUtils.repeat(' ', width - contentWidth - startIndex);
            default:
                return StringUtils.repeat(' ', width - contentWidth) + string;
        }
    }

    /**
     * 获取字符串的展示宽度，英文字符默认展示宽度为1，中文字符默认的展示宽度为2
     *
     * @param value 需要展示的字符
     * @return 字符的展示宽度
     */
    public static int getShowWidth(String value) {
        Validate.notNull(value);
        int length = 0;
        for (char c : value.toCharArray()) {
            length += getShowWidth(c);
        }
        return length;
    }

    /**
     * 获取字符串的展示宽度，英文字符默认展示宽度为1，中文字符默认的展示宽度为2
     *
     * @param value 需要展示的字符
     * @return 字符的展示宽度
     */
    public static int getShowWidth(char value) {
        return StringUtils.isChineseChar(value) ? 2 : 1;
    }

    /**
     * 将表格数据进行格式化输出
     * 如果列的内容为数字，那么右对齐
     * 如果列的内容为文本，那么左对齐
     * 如果列的内容都为等长，那么居中对齐
     *
     * @param dataTable 表格数据
     * @return 格式化输出
     */
    public static String getStringByTable(DataTable dataTable) {
        // 获取每一列的最大展示长度//////////////////////////////////////////////////////
        int[] maxLengths = new int[dataTable.columnCount()];
        int columnCount = dataTable.columnCount();

        for (int i = 0; i < columnCount; i++) {
            maxLengths[i] = getShowWidth(dataTable.columnNames()[i]);
        }

        for (DataRow dataRow : dataTable.dataRows()) {
            for (int i = 0; i < columnCount; i++) {
                String value = dataTableContent2String(dataRow.get(i));
                maxLengths[i] = Math.max(getShowWidth(value), maxLengths[i]);
            }
        }

        //开始组织展示数据/////////////////////////////////////////////////////////////////
        //表头
        StringBuilderExt builder = new StringBuilderExt(2000);
        addSplitLine(builder, maxLengths);
        addContentLine(builder, maxLengths, (Object[]) dataTable.columnNames());
        addSplitLine(builder, maxLengths);

        for (DataRow dataRow : dataTable.dataRows()) {
            addContentLine(builder, maxLengths, dataRow.getValues());
            addSplitLine(builder, maxLengths);
        }

        return builder.toString();
    }

    /**
     * 将表格内容转换为字符串
     *
     * @param object 转换内容
     * @return 转换后的内容
     */
    public static String dataTableContent2String(Object object) {
        if (object == null) {
            return StringUtils.EMPTY;
        }
        String value;
        if (object instanceof Date) {
            value = DateUtils.toShortString((Date) object);
        } else if (object instanceof Number) {
            value = NumberUtils.toString((Number) object);
        } else {
            value = object.toString().trim();
        }
        return value;
    }

    /**
     * 获取分隔行的数据
     */
    private static void addSplitLine(StringBuilderExt builder, int[] maxLengths) {
        builder.append("-+-");
        for (int i : maxLengths) {
            builder.append(StringUtils.repeat('-', i));
            builder.append("-+-");
        }
        builder.appendLine();
    }

    /**
     * 获取内容行的数据
     */
    private static void addContentLine(StringBuilderExt builder, int[] maxLengths, Object... objects) {
        builder.append(" | ");
        for (int i = 0; i < maxLengths.length; i++) {
            Object obj = objects[i];
            String str;
            if (obj == null) {
                str = NULL_STRING;
            } else {
                str = dataTableContent2String(obj);
            }
            builder.append(getStringByFixWidth(str, maxLengths[i]));
            builder.append(" | ");
        }
        builder.appendLine();
    }

}
