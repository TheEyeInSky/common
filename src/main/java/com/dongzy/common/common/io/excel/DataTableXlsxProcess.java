package com.dongzy.common.common.io.excel;

import com.dongzy.common.common.DataGetException;
import com.dongzy.common.common.Validate;
import com.dongzy.common.data.table.DataTable;

/**
 * 采用DataTable来接收excel处理结果
 */
public class DataTableXlsxProcess extends AbstractXlsxProcess {

    private final DataTable dataTable;

    /**
     * 根据传入的参数构造函数
     *
     * @param dataTable       保存读取结果的表格对象
     * @param ignoreEmptyLine 是否跳过空行数据（整行数据都为空）
     */
    public DataTableXlsxProcess(final DataTable dataTable, boolean ignoreEmptyLine) {
        Validate.notNull(dataTable, "datatable不能为null.");
        this.dataTable = dataTable;
        this.ignoreEmptyLine = ignoreEmptyLine;
    }

    @Override
    public void processFirstRow(Object[] values) throws DataGetException {
        String[] columnNames = getColumnName(values);
        dataTable.initColumns(columnNames);
    }

    @Override
    public void processOtherRow(Object[] values) {
        dataTable.append(dataTable.newRow(values.clone()));
    }
}
