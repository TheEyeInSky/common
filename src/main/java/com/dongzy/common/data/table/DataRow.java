package com.dongzy.common.data.table;

import com.dongzy.common.common.Validate;
import com.dongzy.common.common.text.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据行实体类
 */
public class DataRow implements Serializable {

    private final DataTable dataTable;
    private Object[] objects;                               //存储行中的数据对象

    DataRow(final DataTable dataTable) {
        this.dataTable = dataTable;
        objects = new Object[dataTable.getColumnCount()];
    }

    /**
     * 设置行中列的值
     *
     * @param columnName 列名称
     * @param value      列的值
     */
    public void set(String columnName, Object value) {
        if (dataTable.getColumnIndexMap().containsKey(columnName)) {
            objects[dataTable.getColumnIndexMap().get(columnName)] = value;
        } else {
            throw new IndexOutOfBoundsException("数据行中不存在数据列：" + columnName);
        }
    }

    /**
     * 设置行中列的值
     *
     * @param index 列序号
     * @param value 列的值
     */
    public void set(int index, Object value) {
        objects[index] = value;
    }

    /**
     * 获取行中列的值
     *
     * @param columnName 列名
     */
    public Object get(String columnName) {
        if (dataTable.getColumnIndexMap().containsKey(columnName)) {
            return objects[dataTable.getColumnIndexMap().get(columnName)];
        } else {
            throw new IndexOutOfBoundsException("数据行中不存在数据列：" + columnName);
        }
    }

    /**
     * 尝试获取行中列的值，如果列不存在，那么返回null
     *
     * @param columnName 列名
     */
    public Object tryGet(String columnName) {
        return (dataTable.getColumnIndexMap().containsKey(columnName)) ? objects[dataTable.getColumnIndexMap().get(columnName)] : null;
    }

    /**
     * 获取行中列的值
     *
     * @param index 列序号
     */
    public Object get(int index) {
        return objects[index];
    }

    /**
     * 获取行中的所有值
     */
    public Object[] getValues() {
        return objects;
    }

    /**
     * 设置列中的值集合
     *
     * @param values 列中的值的集合
     */
    public void setValues(Object[] values) {
        Validate.isTrue(values.length == dataTable.getColumnCount(), "传入数组长度" + values.length + "不等于表格列数" + dataTable.columnCount());
        this.objects = values;
    }

    /**
     * 设置列中的值集合
     *
     * @param valueMap 列中的值的集合
     */
    public void setValues(Map<String, Object> valueMap) {
        for (String key : valueMap.keySet()) {
            if (StringUtils.notBlank(key)) {       //忽略名称为空的列
                set(key, valueMap.get(key));
            }
        }
    }

    /**
     * 获取主键字段的值
     */
    public Object getPrimaryKey() {
        if (dataTable.getPrimaryColumns() == null || dataTable.getPrimaryColumns().length == 0) {
            throw new IllegalStateException("没有为datatable设置主键字段");
        } else if (dataTable.getPrimaryColumns().length == 1) {
            return get(dataTable.getPrimaryColumns()[0]);
        } else {
            StringBuilder builder = new StringBuilder();
            for (String primaryColumn : dataTable.getPrimaryColumns()) {
                builder.append(StringUtils.toString(get(primaryColumn))).append(",");
            }
            if (builder.length() > 0) {
                builder.setLength(builder.length() - 1);
            }
            return builder.toString();
        }
    }

    /**
     * 获取行数据作为字符串显示时的内容
     */
    public String getNameContent() {
        if (dataTable.getNameColumns() == null || dataTable.getNameColumns().length == 0) {
            throw new IllegalStateException("没有为datatable设置名称字段");
        } else if (dataTable.getNameColumns().length == 1) {
            return StringUtils.toString(get(dataTable.getNameColumns()[0]));
        } else {
            StringBuilder builder = new StringBuilder();
            for (String nameColumn : dataTable.getNameColumns()) {
                builder.append(StringUtils.toString(get(nameColumn))).append(",");
            }
            if (builder.length() > 0) {
                builder.setLength(builder.length() - 1);
            }
            return builder.toString();
        }
    }

    /**
     * 将datarows转换为map对象
     *
     * @return map对象
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        for (String columnName : dataTable.columnNames()) {
            map.put(columnName, this.get(columnName));
        }
        return map;
    }
}
