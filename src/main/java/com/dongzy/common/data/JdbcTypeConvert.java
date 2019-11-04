package com.dongzy.common.data;

import com.dongzy.common.common.Validate;

import java.sql.Types;
import java.text.MessageFormat;

/**
 * JDBC类型转换
 */
public final class JdbcTypeConvert {

    /**
     * 将jdbc数据类型转换为java数据类型
     */
    public static FieldDataEnum toFieldDataType(int jdbcTypeName, int length) {
        switch (jdbcTypeName) {
            case Types.SMALLINT:
            case Types.INTEGER:
                return FieldDataEnum.INTEGER;
            case Types.BIGINT:
                return FieldDataEnum.LONG;
            case Types.BOOLEAN:
            case Types.BIT:
            case Types.TINYINT:
                return FieldDataEnum.BOOLEAN;
            case Types.NCHAR:
            case Types.CHAR:
            case Types.NVARCHAR:
            case Types.VARCHAR:
            case Types.SQLXML:
            case Types.OTHER:
                return FieldDataEnum.STRING;
            case Types.CLOB:
            case Types.NCLOB:
            case Types.LONGNVARCHAR:
            case Types.LONGVARCHAR:
                return FieldDataEnum.CLOB;
            case Types.TIME:
                return FieldDataEnum.TIME;
            case Types.DATE:
                return FieldDataEnum.DATE;
            case Types.TIMESTAMP:
                return FieldDataEnum.TIMESTAMP;
            case Types.DECIMAL:
                return FieldDataEnum.DECIMAL;
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.NUMERIC:
                return (length == 1) ? FieldDataEnum.BOOLEAN : FieldDataEnum.DOUBLE;
            case Types.REAL:
                return FieldDataEnum.FLOAT;
            case Types.BINARY:
            case Types.VARBINARY:
                return FieldDataEnum.BINARY;
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return FieldDataEnum.BLOB;
            default:
                throw new IllegalArgumentException(MessageFormat.format("发现无法识别JDBC的类型：{0}。", jdbcTypeName));
        }
    }

    /**
     * 将jdbc数据类型转换为java数据类型
     *
     * @param javaTypeName java数据类型的名称
     * @return FieldDataType类型
     */
    public static FieldDataEnum toFieldDataType(String javaTypeName) {

        switch (javaTypeName) {
            case "int":
            case "java.lang.Integer":
                return FieldDataEnum.INTEGER;
            case "long":
            case "java.lang.Long":
                return FieldDataEnum.LONG;
            case "boolean":
            case "java.lang.Boolean":
                return FieldDataEnum.BOOLEAN;
            case "string":
            case "java.lang.String":
                return FieldDataEnum.STRING;
            case "java.sql.Date":
                return FieldDataEnum.DATE;
            case "java.sql.Time":
                return FieldDataEnum.TIME;
            case "java.util.Date":
            case "java.sql.Timestamp":
                return FieldDataEnum.TIMESTAMP;
            case "double":
            case "java.lang.Double":
                return FieldDataEnum.DOUBLE;
            case "java.math.BigDecimal":
                return FieldDataEnum.DECIMAL;
            case "float":
            case "java.lang.Float":
                return FieldDataEnum.FLOAT;
            case "byte[]":
                return FieldDataEnum.BLOB;
            case "java.lang.UUID":
                return FieldDataEnum.UUID;
            default:
                throw new IllegalArgumentException(MessageFormat.format("发现无法识别JDBC的类型：{0}。", javaTypeName));
        }
    }

    /**
     * 获取对象对应的数据库类型
     *
     * @param object java数据类型的名称
     * @return FieldDataType类型
     */
    public static FieldDataEnum getObjectDataType(Object object) {
        Validate.notNull(object, "无法获取null值对应的字段类型");
        String className = object.getClass().getName();
        return toFieldDataType(className);
    }
}
