package com.dongzy.common.data;

import com.dongzy.common.common.collection.CollectionUtils;
import com.dongzy.common.common.reflect.PojoAnalysis;
import com.dongzy.common.data.table.DataRow;
import com.dongzy.common.data.table.DataTable;
import com.dongzy.common.log.TextLoggerFactory;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Map集合与数据实体相互转换类，能够实现将Map集合的数据映射到实体，也支持从数据库实体转换为Map对象集合
 * <p>
 * Created by 勇 on 2015/9/18.
 */
public final class EntityConvert {

    private static final Logger LOGGER = TextLoggerFactory.getInstance().getLogger(EntityConvert.class);

    /**
     * 将一个map转换为pojo对象
     *
     * @param map     需要转换的map对象
     * @param myClass 输出的类类型
     * @param <T>     泛型参数
     * @return 实体
     */
    public static <T> T toEntity(Map<String, Object> map, Class<T> myClass) {
        if (map == null) {
            return null;
        }

        PojoAnalysis pojoAnalysis = PojoAnalysis.getPojoAnalysis(myClass);
        try {
            T entity = myClass.newInstance();
            for (Field field : pojoAnalysis.getFields()) {
                Object value = map.get(field.getName());
                if (value != null && pojoAnalysis.getSetMethod(field.getName()) != null) {
                    pojoAnalysis.setValueByMethod(entity, field.getName(), value);
                }
            }
            return entity;
        } catch (Exception e) {
            LOGGER.error("将map对象赋值到实体时发生异常!", e);
            throw new IllegalArgumentException("map对象赋值到实体时发生异常,详情请查看日志文件!");
        }
    }

    /**
     * 将一个listmap对象转换成实体对象的集合
     *
     * @param dataTable 需要转换的数据集合
     * @param myClass   输出的类类型
     * @param <T>       泛型参数
     * @return 实体的集合
     */
    public static <T> Collection<T> toEntities(DataTable dataTable, Class<T> myClass) {
        Collection<T> entities = new LinkedList<>();

        PojoAnalysis pojoAnalysis = PojoAnalysis.getPojoAnalysis(myClass);
        try {
            for (DataRow dataRow : dataTable.dataRows()) {
                T entity = myClass.newInstance();
                for (Field field : pojoAnalysis.getFields()) {
                    Object value = dataRow.tryGet(field.getName());
                    if (value != null && pojoAnalysis.getSetMethod(field.getName()) != null) {
                        pojoAnalysis.setValueByMethod(entity, field.getName(), value);
                    }
                }
                entities.add(entity);
            }
            return entities;
        } catch (Exception e) {
            LOGGER.error("将DataTable对象转换为实体集合时发生异常!", e);
            throw new IllegalArgumentException("将DataTable对象转换为实体集合时发生异常,详情请查看日志文件!");
        }
    }

    /**
     * 将一个实体转换为map对象
     *
     * @param entity 需要转换的实体对象
     * @param <T>    泛型参数
     * @return map对象
     */
    public static <T> Map<String, Object> toMap(T entity) {
        if (entity == null) {
            return null;
        }

        PojoAnalysis pojoAnalysis = PojoAnalysis.getPojoAnalysis(entity.getClass());
        Map<String, Object> map = new HashMap<>();
        try {
            for (Field field : pojoAnalysis.getFields()) {
                if (pojoAnalysis.getGetMethod(field.getName()) != null) {
                    map.put(field.getName(), pojoAnalysis.getValueByMethod(entity, field.getName()));
                }
            }
            return map;
        } catch (Exception e) {
            LOGGER.error("将实体集合对象转换为DataTable时发生异常!", e);
            throw new IllegalArgumentException("将实体集合对象转换为DataTable时发生异常,详情请查看日志文件!");
        }
    }

    /**
     * 将一个对象集合对象转换成DataTable对象
     *
     * @param entities 需要转换的实体集合对象
     * @param <T>      泛型参数
     * @return 实体的集合
     */
    public static <T> DataTable toDataTable(Collection<T> entities) {
        if (entities == null || entities.size() == 0) {
            return null;
        }

        PojoAnalysis pojoAnalysis = PojoAnalysis.getPojoAnalysis(CollectionUtils.getFirst(entities).getClass());
        Collection<Field> fields = new ArrayList<>();
        try {
            for (Field field : pojoAnalysis.getFields()) {
                if (pojoAnalysis.getGetMethod(field.getName()) != null) {
                    fields.add(field);
                }
            }

            DataTable dataTable = new DataTable();
            for (Field field : fields) {
                dataTable.addColumn(field.getName());
                try {
                    FieldDataEnum dataType = JdbcTypeConvert.toFieldDataType(field.getType().getName());
                    dataTable.getColumnTypeMap().put(field.getName(), dataType);
                } catch (IllegalArgumentException ex) {
                    LOGGER.warn("无法识别的jdbc类型", ex);
                }
            }

            for (T entity : entities) {
                DataRow dataRow = dataTable.newRow();
                for (Field field : fields) {
                    if (pojoAnalysis.getGetMethod(field.getName()) != null) {
                        dataRow.set(field.getName(), pojoAnalysis.getValueByMethod(entity, field.getName()));
                    }
                }
                dataTable.append(dataRow);
            }

            return dataTable;
        } catch (Exception e) {
            LOGGER.error("将实体集合对象转换为DataTable时发生异常!", e);
            throw new IllegalArgumentException("将实体集合对象转换为DataTable时发生异常,详情请查看日志文件!");
        }
    }

    /**
     * 将一个对象集合对象转换成DataTable对象
     *
     * @param entities 需要转换的实体集合对象
     * @return 实体的集合
     */
    public static <T> Collection<Map<String, Object>> toMaps(Collection<T> entities) {
        DataTable dataTable = toDataTable(entities);
        return (dataTable == null) ? null : dataTable.toMaps();
    }
}
