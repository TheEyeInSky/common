package com.dongzy.common.data;

/**
 * 数据库字段数据类型枚举类。如：String，int，float等
 * 用户在做查询参数时，我们需要根据参数的不同类型，构件不同的查询参数条件。
 */
public enum FieldDataEnum {
    /**
     * 字符型
     */
    STRING("String", "String", DbDataEnum.STRING),
    /**
     * 长字符串类型
     */
    CLOB("String", "String", DbDataEnum.STRING),
    /**
     * UUID类型
     */
    UUID("UUID", "java.util.UUID", DbDataEnum.STRING),
    /**
     * 整数类型
     */
    INTEGER("Integer", "Integer", DbDataEnum.NUMBER),
    /**
     * 长整形
     */
    LONG("Long", "Long", DbDataEnum.NUMBER),
    /**
     * 布尔型
     */
    BOOLEAN("Boolean", "Boolean", DbDataEnum.NUMBER),
    /**
     * 短整型
     */
    SHORT("Short", "Short", DbDataEnum.NUMBER),
    /**
     * 二进制类型
     */
    BYTE("Byte", "Byte", DbDataEnum.NUMBER),
    /**
     * 单精度浮点数
     */
    FLOAT("Float", "Float", DbDataEnum.NUMBER),
    /**
     * 双精度浮点数
     */
    DOUBLE("Double", "Double", DbDataEnum.NUMBER),
    /**
     * 高精度数据
     */
    DECIMAL("Decimal", "java.math.BigDecimal", DbDataEnum.NUMBER),
    /**
     * 日期类型
     */
    DATE("Date", "java.sql.Date", DbDataEnum.DATE),
    /**
     * 日期类型
     */
    TIME("Date", "java.sql.Time", DbDataEnum.TIME),
    /**
     * 日期类型
     */
    TIMESTAMP("Date", "java.sql.Timestamp", DbDataEnum.DATE),
    /**
     * 标准二进制
     */
    BINARY("Bytes", "byte[]", DbDataEnum.BINARY),
    /**
     * 二进制类型
     */
    BLOB("Bytes", "byte[]", DbDataEnum.BINARY);

    private String methodName;
    private String className;
    private DbDataEnum dbDataEnum;

    /**
     * 根据传入的参数构造函数
     *
     * @param methodName 读写数据时的方法名称
     * @param className  字段对应的java数据类型名称
     * @param dbDataEnum 数据兼容类型
     */
    FieldDataEnum(String methodName, String className, DbDataEnum dbDataEnum) {
        this.methodName = methodName;
        this.className = className;
        this.dbDataEnum = dbDataEnum;
    }

    /**
     * 获取读取数据方法的名称
     *
     * @return 读取数据方法的名称
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * 获取字段类型关联的Java类型名称
     *
     * @return 字段类型关联的Java类型名称
     */
    public String getClassName() {
        return className;
    }

    /**
     * 获取字段的抽象数据类型
     *
     * @return 字段的抽象数据类型
     */
    public DbDataEnum getDbDataEnum() {
        return dbDataEnum;
    }

    /**
     * 返回字段是否兼容
     *
     * @param fieldDataEnum 字符数据类型
     * @return 是否兼容
     */
    public boolean isCompatible(FieldDataEnum fieldDataEnum) {
        return this.dbDataEnum == fieldDataEnum.dbDataEnum;
    }

}
