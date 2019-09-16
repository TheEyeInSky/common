package com.dongzy.common.common;

import com.dongzy.common.common.builder.EqualsBuilder;
import com.dongzy.common.common.builder.HashCodeBuilder;
import com.dongzy.common.common.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * 数据项类
 */
public class DataItem<T, V> implements Serializable {

    private T name;            //字典名称
    private V value;           //字典值，一般是字符串，可能是表的主键值

    /**
     * 默认构造函数
     */
    public DataItem() {
        super();
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param name  字典名称
     * @param value 字典值
     */
    public DataItem(T name, V value) {
        Validate.notNull(name, "name is null!");
        Validate.notNull(value, "value is null!");

        this.name = name;
        this.value = value;
    }

    public T getName() {
        return name;
    }

    public V getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, true);
    }
}
