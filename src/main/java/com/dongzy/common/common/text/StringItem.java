package com.dongzy.common.common.text;

import com.dongzy.common.common.builder.ToStringBuilder;

import java.util.Map;

/**
 * Created by zouyong on 2018/1/21.
 */
public class StringItem<T> implements Map.Entry<String, T> {

    private String key;
    private T value;

    public StringItem(String key, T value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public java.lang.String getKey() {
        return key;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public T setValue(T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
