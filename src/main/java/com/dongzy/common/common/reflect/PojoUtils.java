package com.dongzy.common.common.reflect;

import com.dongzy.common.common.Validate;
import com.dongzy.common.common.collection.ArrayUtils;
import com.dongzy.common.common.collection.CollectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Pojo的工具类，
 */
public class PojoUtils {

    /**
     * 将原对象中的字段值，复制到目的类上
     */
    public static <T> T copyProperties(Object source, Class<T> cl) {
        Validate.notNull(cl, "目标对象类型不能为空！");
        try {
            T objective = cl.newInstance();
            copyProperties(source, objective);
            return objective;
        } catch (Exception e) {
            throw new IllegalArgumentException("无法创建" + cl + "的实例", e);
        }
    }

    /**
     * 将原对象中的字段值，复制到目的类上
     */
    public static void copyProperties(Object source, Object objective) {
        copyProperties(source, objective, false);
    }

    /**
     * 将原对象中的字段值，复制到目的类上
     */
    public static void copyProperties(Object source, Object objective, String... ignoreFields) {
        copyProperties(source, objective, false, ignoreFields);
    }

    /**
     * 将原对象中的字段值，复制到目的类上     *
     * 如果字段类型兼容，会进行适当的类型转换，否则将会抛出异常
     * <p>
     * 如果两个对象类型完全一致，那么将会直接采用字段值复制的方式，
     * 如果两个对象类型不一致，那么将会调用get和set方法进行复制，如果无法找到对应的get和set方法，那么将会忽略该字段的赋值操作
     *
     * @param source       被复制对象
     * @param objective    被赋值的新对象
     * @param ignoreNull   是否忽略null值，如果为true的话，原始类中null会被忽略不复制到目标类中（默认为false）
     * @param ignoreFields 忽略跳过的字段
     * @throws IllegalArgumentException 如果对应字段的类型无法读取或转换成功，将会抛出此异常
     */
    public static void copyProperties(Object source, Object objective, boolean ignoreNull, String... ignoreFields) {
        Validate.notNull(source, "原对象不能为null");
        Validate.notNull(objective, "目标对象不能为null");

        try {
            if (source.getClass().equals(objective.getClass())) {
                PojoAnalysis pojoAnalysis = PojoAnalysis.getPojoAnalysis(source.getClass());
                for (Field field : pojoAnalysis.getFields()) {
                    Object filedValue = field.get(source);
                    if ((!ignoreNull || filedValue != null) && !ArrayUtils.contains(ignoreFields, field.getName())) {
                        field.set(objective, filedValue);
                    }
                }
            } else {
                PojoAnalysis sourceAnalysis = PojoAnalysis.getPojoAnalysis(source.getClass());
                PojoAnalysis objectiveAnalysis = PojoAnalysis.getPojoAnalysis(objective.getClass());
                Collection<String> fieldNames = CollectionUtils.intersection(sourceAnalysis.getRwFieldNames(), objectiveAnalysis.getRwFieldNames());

                for (String fieldName : fieldNames) {
                    Object filedValue = sourceAnalysis.getValueByMethod(source, fieldName);
                    if ((!ignoreNull || filedValue != null) && !ArrayUtils.contains(ignoreFields, fieldName)) {
                        objectiveAnalysis.setValueByMethod(objective, fieldName, filedValue);
                    }
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("复制对象属性值时发生异常！", e);
        }
    }
}
