package com.dongzy.common.data.table;

import com.dongzy.common.data.FieldDataEnum;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import com.dongzy.common.common.Validate;
import com.dongzy.common.common.collection.ArrayUtils;
import com.dongzy.common.common.collection.CollectionUtils;
import com.dongzy.common.common.text.FormatData;
import com.dongzy.common.common.text.StringUtils;
import com.dongzy.common.data.FieldDataEnum;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 数据表的数据，对象拥有固定的行和列
 */
public class DataTable implements Serializable {

    private String TableName;                           //存储表格的名称
    private String[] columnNames;
    private int columnCount;
    private List<DataRow> dataRowList;
    private Map<String, String> columnTitleMap;         //存储列于标题的对应关系，title为可选内容
    private Map<String, Integer> columnIndexMap;        //存储列名与序号的对应关系
    private Map<String, FieldDataEnum> columnTypeMap;   //存储列名与对应的数据类型关系
    private Lock lock = new ReentrantLock();            //添加锁，确保数据一致性
    private boolean autoAddColumn = false;              //当添加不存在的列数据时，是否自动增加新列，默认为false
    private boolean biggerData;                         //是否用于保存大数据，大数据采用linked，小数据采用arraylist
    private String[] primaryColumns;                    //表的主键字段集合
    private String[] nameColumns;                       //行数据作为字符串显示时的字段集合

    /**
     * 默认构造函数
     */
    public DataTable() {
        initColumns(new String[0]);
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param columnNames 列名称集合
     */
    public DataTable(String[] columnNames) {
        initColumns(columnNames);
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param mapCollection 用于初始化表格的map集合ß
     */
    public DataTable(Collection<Map<String, Object>> mapCollection) {
        this(CollectionUtils.toArray(CollectionUtils.getFirst(mapCollection).keySet()));
        dataRowList = new ArrayList<>(mapCollection.size());
        for (Map<String, Object> map : mapCollection) {
            append(newRow(map));
        }
    }

    /**
     * 获取列名和序号对应关系
     */
    Map<String, Integer> getColumnIndexMap() {
        return columnIndexMap;
    }

    /**
     * 获取列名对应的数据类型
     *
     * @return 列对应的数据类型
     */
    public Map<String, FieldDataEnum> getColumnTypeMap() {
        return columnTypeMap;
    }

    /**
     * 获取表格总列数
     */
    int getColumnCount() {
        return columnCount;
    }

    /**
     * 获取是否用于保存大数据对象
     */
    public boolean isBiggerData() {
        return biggerData;
    }

    /**
     * 设置是否用于保存大数据对象
     */
    public void setBiggerData(boolean biggerData) {
        this.biggerData = biggerData;
        processBiggerData();
    }

    /**
     * 获取指定列的标题
     */
    public String getColumnTitle(String columnName) {
        return columnTitleMap.getOrDefault(columnName, columnName);
    }

    /**
     * 设置列的标题
     *
     * @param columnName 列名
     * @param title      列标题
     */
    public void setColumnTitle(String columnName, String title) {
        columnTitleMap.put(columnName, title);
    }

    public String[] getPrimaryColumns() {
        return primaryColumns;
    }

    public void setPrimaryColumns(String... primaryColumns) {
        this.primaryColumns = primaryColumns;
    }

    public String[] getNameColumns() {
        return nameColumns;
    }

    public void setNameColumns(String... nameColumns) {
        this.nameColumns = nameColumns;
    }

    /**
     * 配置存储结构为制定的结构     *
     */
    private void processBiggerData() {
        if (dataRowList == null) {
            dataRowList = (biggerData) ? new LinkedList<>() : new ArrayList<>();
        } else {
            if (biggerData) {
                if (!(dataRowList instanceof LinkedList)) {
                    List<DataRow> newDataRows = new LinkedList<>();
                    newDataRows.addAll(dataRowList);
                    dataRowList = newDataRows;
                }
            } else {
                if (!(dataRowList instanceof ArrayList)) {
                    dataRowList = new ArrayList<>(dataRowList);
                }
            }
        }
    }

    /**
     * 获取表格的名称
     *
     * @return 表格名称
     */
    public String getTableName() {
        return TableName;
    }

    /**
     * 设置表格的名称
     *
     * @param tableName 表格名称
     */
    public void setTableName(String tableName) {
        TableName = tableName;
    }

    /**
     * 设置当添加不存在的列数据时，是否自动增加新列，默认为false
     *
     * @param autoAddColumn 是否自动增加新列
     */
    public void setAutoAddColumn(boolean autoAddColumn) {
        this.autoAddColumn = autoAddColumn;
    }

    /**
     * 获取当添加不存在的列数据时，是否自动增加新列，默认为false
     *
     * @return 是否自动增加列
     */
    public boolean isAutoAddColumn() {
        return autoAddColumn;
    }

    /**
     * 初始化表格数据列，如果原来有数据，那么将会被全部删除
     *
     * @param columnNames 初始化的列集合
     */
    public synchronized void initColumns(String[] columnNames) {
        //清空原有的数据
        if (dataRowList != null) {
            dataRowList.clear();
            processBiggerData();
        } else {
            dataRowList = (biggerData) ? new LinkedList<>() : new ArrayList<>();
        }
        columnIndexMap = new CaseInsensitiveMap<>();
        columnTypeMap = new CaseInsensitiveMap<>();
        columnTitleMap = new CaseInsensitiveMap<>();
        this.columnNames = new String[0];

        addColumn(columnNames);
    }

    /**
     * 是否包含此列
     */
    public boolean containsColumn(String columnName) {
        if (StringUtils.isBlank(columnName)) {
            return false;
        }
        return columnIndexMap.containsKey(processColumnName(columnName));
    }

    /**
     * 为DataTable添加新的列
     *
     * @param columnNames 需要添加的列集合
     */
    public void addColumn(String... columnNames) {
        if (ArrayUtils.isEmpty(columnNames)) {
            return;
        }

        columnNames = processColumnNames(columnNames);

        for (int i = 0; i < columnNames.length; i++) {
            columnNames[i] = columnNames[i];
        }

        lock.lock();
        try {
            //将现有的列初始化
            Collection<String> names = new ArrayList<>(columnCount + columnNames.length);
            //获取所有名字不为空的列
            names.addAll(Arrays.asList(this.columnNames));

            //添加新的列
            int index = columnCount;
            boolean isAddColumn = false;
            for (String columnName : columnNames) {
                if (StringUtils.isBlank(columnName)) {     //如果名为空，那么直接跳过
                    continue;
                }
                if (columnIndexMap.containsKey(columnName)) {       //如果不包含该列名，那么添加
                    continue;
                }

                columnIndexMap.put(columnName, index);
                index++;
                names.add(columnName);
                isAddColumn = true;
            }

            if (isAddColumn) {      //如果有新增的列，那么
                columnCount = index;
                this.columnNames = CollectionUtils.toArray(names);

                //所有数据行扩展列
                for (DataRow dataRow : dataRows()) {
                    Object[] objects = Arrays.copyOf(dataRow.getValues(), columnCount);
                    dataRow.setValues(objects);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 从数据表中移除列
     *
     * @param columnNames 需要移除的列集合
     */
    public void removeColumn(String... columnNames) {
        if (ArrayUtils.isEmpty(columnNames)) {
            return;
        }

        columnNames = processColumnNames(columnNames);

        lock.lock();
        try {
            //取补集，拿到最新的列名
            Collection<String> names = CollectionUtils.subtract(ArrayUtils.toCollection(this.columnNames),
                    ArrayUtils.toCollection(columnNames));
            this.columnNames = CollectionUtils.toArray(names);

            Collection<DataRow> oldRows = new LinkedList<>();
            oldRows.addAll(this.dataRowList);

            Map<String, Integer> oldColumnMap = new CaseInsensitiveMap<>(columnIndexMap);

            //更新列映射的数据
            columnIndexMap.clear();
            columnCount = this.columnNames.length;
            for (int i = 0; i < columnCount; i++) {
                columnIndexMap.put(this.columnNames[i], i);
            }

            //更新列的数据
            this.dataRowList.clear();

            for (DataRow dataRow : oldRows) {
                DataRow newRow = this.newRow();
                for (String name : names) {
                    newRow.set(name, dataRow.get(oldColumnMap.get(name)));
                }
                this.append(newRow);
            }

            oldRows.clear();


        } finally {
            lock.unlock();
        }
    }

    /**
     * 创建一个新行，注意不会将新行添加到表格中
     *
     * @return 返回新行
     */
    public DataRow newRow() {
        return new DataRow(this);
    }

    /**
     * 创建一个新行，注意不会将新行添加到表格中
     *
     * @return 返回新行
     */
    public DataRow newRow(Object[] values) {
        DataRow dataRow = new DataRow(this);
        dataRow.setValues(values);
        return dataRow;
    }

    /**
     * 创建一个新行，注意不会将新行添加到表格中
     *
     * @return 返回新行
     */
    public DataRow newRow(Map<String, Object> valueMap) {
        DataRow dataRow = new DataRow(this);
        try {
            dataRow.setValues(valueMap);
        } catch (IndexOutOfBoundsException ex) {        //如果发生此异常，表示数据表缺少咧
            if (autoAddColumn) {
                addColumn(CollectionUtils.toArray(valueMap.keySet()));
                dataRow = new DataRow(this);
                dataRow.setValues(valueMap);
            } else {
                throw ex;
            }
        }
        return dataRow;
    }

    /**
     * 获取表格中所有的数据行
     */
    public List<DataRow> dataRows() {
        return dataRowList;
    }

    /**
     * 获取表格的总行数
     */
    public int rowCount() {
        return dataRowList.size();
    }

    /**
     * 获取表格的总列数
     */
    public int columnCount() {
        return columnCount;
    }

    /**
     * 获取所有的列名称，从安全性的角度考虑，采用深拷贝
     */
    public String[] columnNames() {
        return columnNames.clone();
    }

    /**
     * 在表格的最后添加数据行
     *
     * @param dataRow 需要新增的数据行
     */
    public void append(DataRow dataRow) {
        dataRowList.add(dataRow);
    }

    /**
     * 在表格的最后添加数据行集
     *
     * @param dataRows 需要新增的数据行集
     */
    public void append(Collection<DataRow> dataRows) {
        dataRowList.addAll(dataRows);
    }

    /**
     * 在特定的位置插入行
     *
     * @param index   需要插入的位置
     * @param dataRow 数据行
     */
    public void insert(int index, DataRow dataRow) {
        dataRowList.add(index, dataRow);
    }

    /**
     * 移除特定的数据行
     *
     * @param dataRow 行对象
     */
    public void remove(DataRow dataRow) {
        dataRowList.remove(dataRow);
    }

    /**
     * 移除特定的数据行
     *
     * @param index 行号
     */
    public void remove(int index) {
        dataRowList.remove(index);
    }

    /**
     * 清空整个数据表
     */
    public void clear() {
        dataRowList.clear();
    }

    /**
     * 将表格对象转换为Map集合对象
     *
     * @return Map集合对象
     */
    public Collection<Map<String, Object>> toMaps() {
        Collection<Map<String, Object>> maps = new LinkedList<>();
        for (DataRow dataRow : dataRows()) {
            Map<String, Object> map = new HashMap<>();
            for (String columnName : columnNames) {
                map.put(columnName, dataRow.get(columnName));
            }
            maps.add(map);
        }
        return maps;
    }

    /**
     * 变更列的名称
     *
     * @param oldName 原来的名称
     * @param newName 新的名称
     */
    public void renameColumn(String oldName, String newName) {
        oldName = processColumnName(oldName);
        newName = processColumnName(newName);
        Validate.isTrue(columnIndexMap.containsKey(oldName), "没有找到需要替换的列名:" + oldName);
        int index = columnIndexMap.get(oldName);
        renameColumn(index, newName);
    }

    /**
     * 变更列的名称
     *
     * @param index   列的序号
     * @param newName 新的名称
     */
    public void renameColumn(int index, String newName) {
        newName = processColumnName(newName);
        Validate.isTrue(!columnIndexMap.keySet().contains(newName), "列名:" + newName + " 已经存在，无法重复添加!");
        String oldName = columnNames[index];
        columnIndexMap.remove(oldName);
        columnNames[index] = newName;
        columnIndexMap.put(newName, index);

        FieldDataEnum type = columnTypeMap.get(oldName);
        columnTypeMap.remove(oldName);
        columnTypeMap.put(newName, type);
    }

    /**
     * 按照指定的字段集合进行排序
     *
     * @param asc        采用升序还是降序排列
     * @param columnName 需要配需的字段
     */
    public void sort(String columnName, boolean asc) {
        columnName = processColumnName(columnName);
        sort(new String[]{columnName}, new boolean[]{asc});
    }

    /**
     * 按照指定的字段集合进行排序，
     * columnNames靠前的字段排序优先级更高
     * sorts为排序方向（true升序，false降序）
     *
     * @param columnNames 需要配需的字段集合
     * @param sorts       升序降序组合
     */
    public void sort(String[] columnNames, boolean[] sorts) {
        columnNames = processColumnNames(columnNames);
        Validate.notNull(sorts);

        Validate.isTrue(columnNames.length == sorts.length, "排序字段与排序方向数组长度不一致");
        if (columnNames.length == 0) {
            return;
        }

        int columnCount = columnNames.length;
        // 1.取出来需要排序的列的唯一值，并放到treeset中自动排序/////////////////////////////////////
        List<TreeMap<String, Integer>> treeMaps = new ArrayList<>(columnCount);
        for (String ignored : columnNames) {
            treeMaps.add(new TreeMap<>());
        }
        // 1.1 把唯一值写入treeset中
        for (DataRow dataRow : dataRows()) {
            for (int i = 0; i < columnCount; i++) {
                treeMaps.get(i).put(StringUtils.toString(dataRow.get(columnNames[i])), 0);
            }
        }
        // 1.2 根据顺序，写入key的排序需要作为value值
        for (int i = 0; i < columnCount; i++) {
            int index = 1;
            for (Map.Entry<String, Integer> entry : treeMaps.get(i).entrySet()) {
                entry.setValue(index++);
            }
        }

        // 2.得到每一列的权重值
        double[] weights = new double[columnCount];
        weights[weights.length - 1] = 1;            //最后一项的权重为1
        double weightValue = 1;
        for (int i = columnCount - 2; i >= 0; i--) {
            //每一项的权重为前一项与数量乘积
            weightValue = weightValue * treeMaps.get(i + 1).size();
            //最终权限数据需要在此基础上再增加1来确保大于上一级的最大值
            weights[i] = weightValue + 1;
        }

        // 3.遍历数据，得到每一行数据的权重值，并存储结果集中
        Map<Long, Collection<DataRow>> dataRowMap = new TreeMap<>();
        for (DataRow dataRow : dataRows()) {
            long weight = 0;
            for (int i = 0; i < columnCount; i++) {
                //计算：序号 * 权重 * 升降序
                weight += treeMaps.get(i).get(StringUtils.toString(dataRow.get(columnNames[i]))) * weights[i] * (sorts[i] ? 1 : -1);
            }
            Collection<DataRow> collection = dataRowMap.computeIfAbsent(weight, k -> new ArrayList<>());
            collection.add(dataRow);
        }

        // 3.更新表格的现有数据内容
        clear();
        for (Map.Entry<Long, Collection<DataRow>> entry : dataRowMap.entrySet()) {
            append(entry.getValue());
        }

        //尝试调用垃圾回收
        System.gc();
    }

    @Override
    public String toString() {
        return FormatData.getStringByTable(this);
    }

    /**
     * 将表格内容转换为适合展示的内容格式
     *
     * @return 输出适合展示的表格数据
     */
    public DataTable toShowDataTable() {
        DataTable dataTable = new DataTable(this.columnNames);
        for (DataRow dataRow : this.dataRowList) {
            DataRow newRow = dataTable.newRow();
            Object[] values = dataRow.getValues();
            for (int i = 0; i < this.columnCount; i++) {
                newRow.set(i, FormatData.dataTableContent2String(values[i]));
            }
            dataTable.append(newRow);
        }
        return dataTable;
    }

    /**
     * 处理和检查单个字段
     *
     * @param columnName 字段名称
     * @return 处理后的结果
     */
    private String processColumnName(String columnName) {
        Validate.notBlank(columnName, "列名不能为空");
        return columnName.trim();
    }

    /**
     * 处理和检查多个字段
     *
     * @param columnNames 字段名称
     * @return 处理后的结果
     */
    private String[] processColumnNames(String[] columnNames) {
        Validate.notEmpty(columnNames, "列名不能为空");
        List<String> strings = new ArrayList<>(columnNames.length);
        for (String columnName : columnNames) {
            if (StringUtils.notBlank(columnName)) {
                strings.add(columnName.trim());
            }
        }
        return (strings.size() > 0) ? CollectionUtils.toArray(strings) : new String[0];
    }
}
