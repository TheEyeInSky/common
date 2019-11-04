package com.dongzy.common.common.reflect;

import com.dongzy.common.common.BooleanUtils;
import com.dongzy.common.common.UuidUtils;
import com.dongzy.common.common.Validate;
import com.dongzy.common.common.VariableUtils;
import com.dongzy.common.common.collection.CollectionUtils;
import com.dongzy.common.common.time.DateUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反射获取实体字段相关信息的类
 */
public final class PojoAnalysis {

    private static final Map<String, PojoAnalysis> POJO_ANALYSIS_MAP = new ConcurrentHashMap<>();

    private Class<?> cl;                                    //类的名称
    private List<String> fieldNames;                        //所有字段的集合
    private List<String> rwFieldNames;                      //同时具有读写方法的字段集合
    private Map<String, Field> fieldMap;                    //所有字段的集合
    private Map<String, Method> getMethodMap;               //get方法集合
    private Map<String, Method> setMethodMap;               //set方法集合
    private int maxModifier = 4;                            //默认值返回修复符号小于等于4的field
    private Collection<Integer> modifiers;

    private PojoAnalysis(Class<?> cl) {
        this.cl = cl;
        initPojoAnalysis();
    }

    private PojoAnalysis(Class<?> cl, Collection<Integer> modifiers) {
        this(cl);
        Validate.notEmpty(modifiers, "修复符集合不能为空");
        this.modifiers = modifiers;
        initPojoAnalysis();
    }

    private PojoAnalysis(Class<?> cl, int maxModifier) {
        this(cl);
        Validate.isTrue(maxModifier > 0, "修复符限定值必须大于零");
        this.maxModifier = maxModifier;
        initPojoAnalysis();
    }

    /**
     * 获取PojoAnalysis对象
     *
     * @param cl 需要分析pojo类型
     * @return PojoAnalysis对象
     */
    public static PojoAnalysis getPojoAnalysis(Class<?> cl) {
        PojoAnalysis pojoAnalysis = POJO_ANALYSIS_MAP.get(cl.getName());
        if (pojoAnalysis == null) {
            pojoAnalysis = new PojoAnalysis(cl);
            POJO_ANALYSIS_MAP.put(cl.getName(), new PojoAnalysis(cl));
        }
        return pojoAnalysis;
    }

    /**
     * 获取PojoAnalysis对象
     *
     * @param cl        需要分析pojo类型
     * @param modifiers 值返回集合范围内的修饰符对应的字段集合
     * @return PojoAnalysis对象
     */
    public static PojoAnalysis getPojoAnalysis(Class<?> cl, Collection<Integer> modifiers) {
        String key = String.format("%s###%s", cl, modifiers);
        PojoAnalysis pojoAnalysis = POJO_ANALYSIS_MAP.get(key);
        if (pojoAnalysis == null) {
            pojoAnalysis = new PojoAnalysis(cl, modifiers);
            POJO_ANALYSIS_MAP.put(key, pojoAnalysis);
        }
        return pojoAnalysis;
    }

    /**
     * 获取PojoAnalysis对象
     *
     * @param cl          需要分析pojo类型
     * @param maxModifier 指定获取field的修复符的最大值，大于此值得字段将不会被列出来
     * @return PojoAnalysis对象
     */
    public static PojoAnalysis getPojoAnalysis(Class<?> cl, int maxModifier) {
        String key = String.format("%s###%s", cl, maxModifier);
        PojoAnalysis pojoAnalysis = POJO_ANALYSIS_MAP.get(key);
        if (pojoAnalysis == null) {
            pojoAnalysis = new PojoAnalysis(cl, maxModifier);
            POJO_ANALYSIS_MAP.put(key, pojoAnalysis);
        }
        return pojoAnalysis;
    }

    private void initPojoAnalysis() {

        this.fieldNames = new ArrayList<>();
        this.rwFieldNames = new ArrayList<>();
        this.fieldMap = new LinkedHashMap<>();
        this.getMethodMap = new LinkedHashMap<>();
        this.setMethodMap = new LinkedHashMap<>();
        analysisField(this.cl);
        analysisMethod(this.cl);

        //标记所有的可读写字段
        for (String fieldName : fieldNames) {
            if (getGetMethod(fieldName) != null && getSetMethod(fieldName) != null) {
                this.rwFieldNames.add(fieldName);
            }
        }

    }

    /**
     * 获取类的所有字段名称的集合
     *
     * @return 字段名称集合
     */
    public List<String> getFieldNames() {
        return fieldNames;
    }

    /**
     * 获取类中同时具有get和set方法的字段集合
     *
     * @return 字段名称集合
     */
    public List<String> getRwFieldNames() {
        return rwFieldNames;
    }

    /**
     * 获取字段
     *
     * @param fieldName 字段名称
     * @return 字段
     */
    public Field getField(String fieldName) {
        return fieldMap.get(fieldName);
    }

    /**
     * 获取所有的字段（包括继承字段）
     *
     * @return 所有字段集合
     */
    public List<Field> getFields() {
        return new ArrayList<>(fieldMap.values());
    }

    /**
     * 获取指定字段的读方法
     *
     * @param fieldName 字段名称
     * @return 读方法
     */
    public Method getGetMethod(String fieldName) {
        return getMethodMap.get(fieldName);
    }

    /**
     * 获取类的写方法
     *
     * @param fieldName 字段名称
     * @return 写方法
     */
    public Method getSetMethod(String fieldName) {
        return setMethodMap.get(fieldName);
    }

    /**
     * 获取字段的值，读取的时候通过get方法来获取
     *
     * @param t         需要读取值得类的实例
     * @param fieldName 字段名称
     * @return 字段的值
     */
    public <T> Object getValueByMethod(T t, String fieldName) throws ReflectiveOperationException {
        Validate.notEmpty(fieldName);
        Method method = getMethodMap.get(fieldName);
        Validate.notNull(method, String.format("无法在类%s中找到字段%s的get方法！", cl.getName(), fieldName));
        return method.invoke(t);
    }

    /**
     * 获取字段的值，读取的时候直接读取字段的值，而不通过get方法
     *
     * @param fieldName 字段名称
     * @return 字段的值
     */
    public <T> Object getValueByField(T t, String fieldName) throws ReflectiveOperationException {
        Validate.notEmpty(fieldName);
        Field field = fieldMap.get(fieldName);
        return getValueByField(t, field);
    }

    /**
     * 获取字段的值，读取的时候直接读取字段的值，而不通过get方法
     *
     * @param field 字段实体类
     * @return 字段的值
     */
    public <T> Object getValueByField(T t, Field field) throws ReflectiveOperationException {
        return field.get(t);
    }

    /**
     * 设置字段的值，通过类的set方法将值写入
     *
     * @param t         需要设置的实体类
     * @param fieldName 字段名称
     * @param value     需要设置的属性值
     */
    public <T> void setValueByMethod(T t, String fieldName, Object value) throws ReflectiveOperationException {

        Validate.notEmpty(fieldName);
        Method method = setMethodMap.get(fieldName);
        Validate.notNull(method, String.format("无法在类%s中找到字段%s的set方法！", cl.getName(), fieldName));

        Object writeValue = convertSetValue(method.getParameterTypes()[0].getName(), value);
        method.invoke(t, writeValue);
    }

    /**
     * 设置字段的值，直接写入类的值，不走set方法
     *
     * @param t         需要设置的实体类
     * @param fieldName 字段名称
     * @param value     需要设置的属性值
     */
    public <T> void setValueByField(T t, String fieldName, Object value) throws ReflectiveOperationException {
        Validate.notEmpty(fieldName);
        Field field = fieldMap.get(fieldName);
        setValueByField(t, field, value);
    }

    /**
     * 设置字段的值，直接写入类的值，不走set方法
     *
     * @param t     需要设置的实体类
     * @param field 字段类
     * @param value 需要设置的属性值
     */
    public <T> void setValueByField(T t, Field field, Object value) throws ReflectiveOperationException {
        Object writeValue = convertSetValue(field.getType().getName(), value);
        field.set(t, writeValue);
    }

    // 初始化所有的元数据
    private synchronized void analysisField(Class<?> cl) {
        Validate.notNull(cl);

        Field[] fields = cl.getDeclaredFields();

        for (Field field : fields) {

            if (CollectionUtils.isEmpty(modifiers)) {
                if (field.getModifiers() > maxModifier) {
                    continue;
                }
            } else {
                if (!modifiers.contains(field.getModifiers())) {
                    continue;
                }
            }

            //添加未被添加过的字段
            if (!fieldMap.containsKey(field.getName())) {
                fieldMap.put(field.getName(), field);
                fieldNames.add(field.getName());
            }
            //允许直接读写字段
            field.setAccessible(true);
        }

        //递归获取父类的字段信息
        Class<?> superClass = cl.getSuperclass();
        if (superClass != null && !superClass.equals(Object.class)) {
            analysisField(superClass);
        }
    }


    // 初始化所有的元数据
    private synchronized void analysisMethod(Class<?> cl) {
        Validate.notNull(cl);

        for (Field field : getFields()) {

            //获取get方法
            Method getMethod = null;
            try {
                getMethod = cl.getMethod(VariableUtils.buildGetMethodName(field.getName(), false));
            } catch (NoSuchMethodException e) {
                //布尔类型的get和set存在特殊逻辑
                if (field.getType().getName().toLowerCase().endsWith("boolean")) {
                    try {       //按照标准的boolean前面添加is前缀去找
                        getMethod = cl.getMethod(VariableUtils.buildGetMethodName(field.getName(), true));
                    } catch (NoSuchMethodException e1) {
                        //TODO
                    }
                }
            }
            if (getMethod != null && !getMethodMap.containsKey(field.getName())) {
                getMethodMap.put(field.getName(), getMethod);
            }

            //获取set方法
            try {
                Method setMethod = cl.getMethod(VariableUtils.buildSetMethodName(field.getName(), field.getType().equals(Boolean.class)), field.getType());
                //添加字段关联的set方法
                if (!setMethodMap.containsKey(field.getName())) {
                    setMethodMap.put(field.getName(), setMethod);
                }
            } catch (NoSuchMethodException e) {
                //TODO
            }
        }

        //递归获取父类的字段信息
        Class<?> superClass = cl.getSuperclass();
        if (superClass != null && !superClass.equals(Object.class)) {
            analysisMethod(superClass);
        }
    }

    /**
     * 将需要set的数据类型转换为适当的类型，
     * 比如字段类型为int，但是传入的是“1”，那么也要确保1能够被写入
     *
     * @return 适合的值
     */
    private Object convertSetValue(String typeName, Object value) {
        //如果是空值，那么无需写入
        if (value == null) {
            return null;
        }

        switch (typeName) {
            case "java.lang.String":
                if (!(value instanceof String)) {
                    return value.toString();
                }
                break;
            case "java.util.Date":
                return DateUtils.tryToDate(value);
            case "java.sql.Timestamp":
                Date date;
                if (value instanceof Date) {
                    date = (Date) value;
                } else {
                    date = DateUtils.tryToDate(value);
                }
                return (date == null) ? null : new Timestamp(date.getTime());
            case "java.lang.Integer":
            case "int":
                if (!(value instanceof Integer)) {
                    return Integer.parseInt(value.toString());
                }
                break;
            case "java.lang.Double":
            case "double":
                if (!(value instanceof Double)) {
                    return Double.parseDouble(value.toString());
                }
                break;
            case "java.lang.Float":
            case "float":
                if (!(value instanceof Float)) {
                    return Float.parseFloat(value.toString());
                }
                break;
            case "java.lang.Short":
            case "short":
                if (!(value instanceof Short)) {
                    return Short.parseShort(value.toString());
                }
                break;
            case "java.lang.Long":
            case "long":
                if (!(value instanceof Long)) {
                    return Long.parseLong(value.toString());
                }
                break;
            case "java.lang.Byte":
            case "byte":
                if (!(value instanceof Byte)) {
                    return Byte.parseByte(value.toString());
                }
                break;
            case "java.math.BigDecimal":
                if (!(value instanceof BigDecimal)) {
                    return BigDecimal.valueOf(Double.parseDouble(value.toString()));
                }
                break;
            case "java.lang.Boolean":
                if (!(value instanceof Boolean)) {
                    return BooleanUtils.toBoolean(value.toString());
                }
                break;
            case "boolean":
                if (!(value instanceof Boolean)) {
                    return BooleanUtils.tryToBoolean(value.toString());
                }
                break;
            case "java.util.UUID":
                if (!(value instanceof UUID)) {
                    return UuidUtils.toUUID(value.toString());
                }
                break;
            default:
                //TODO
        }

        return value;
    }
}
