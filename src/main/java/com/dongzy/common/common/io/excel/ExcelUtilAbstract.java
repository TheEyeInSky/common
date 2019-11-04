package com.dongzy.common.common.io.excel;

import com.dongzy.common.common.DataGetException;
import com.dongzy.common.common.io.WriteFileAbstract;
import com.dongzy.common.common.text.StringBuilderExt;
import com.dongzy.common.common.text.StringUtils;
import com.dongzy.common.common.time.DateUtils;
import com.dongzy.common.data.table.DataRow;
import com.dongzy.common.data.table.DataTable;
import com.dongzy.common.log.TextLoggerFactory;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;

/**
 * 用于读写Excel的辅助类，只能处理标准xls文件或者体积较小的xlsx文件
 * 建议xlsx文件的记录条数小于65536条，列数少于256列
 * 如果处理更大的文件，应该采用BigExcelUtil工具来进行处理。
 */
public abstract class ExcelUtilAbstract extends WriteFileAbstract<DataTable> {

    private static final Logger LOGGER = TextLoggerFactory.getInstance().getLogger(XlsReader.class);
    public final static String STRING_FOMMAT = "@";             //文本格式
    public final static String LONG_FOMMAT = "#,##0";           //不带小数的数字，每三位数字用逗号分隔
    public final static String INT_FOMMAT = "#0";               //不带小数的数字
    public final static String DATE_FOMMAT = "yyyy/mm/dd";      //日期格式
    public final static String LONG_DATE_FOMMAT = "yyyy/mm/dd hh:mm:ss";      //日期格式
    public final static String PERCENTAGE_FOMMAT = "#0%";       //不带小数的百分比格式
    public final static String DOUBLE_FOMMAT = "#0.00";         //带两位小数的数字格式
    protected ExcelDocumentEnum excelDocumentEnum;

    protected Workbook workbook;
    /**
     * 是否忽略读取过程中的错误信息;
     * 如果为是，那么遇到错误将会抛出异常，
     * 如果为否，那么会将异常写入警告日志，不会阻止读写操作
     */
    protected boolean ignoreError = false;
    /**
     * 是否删除空行的数据，（整行为空的数据）
     */
    private boolean ignoreEmptyLine = true;

    //保存每一列数据对应的存储类型，会影响到数据写入的excel文件时的表现形式
    private Map<String, String> columnTypeMap = new HashMap<>();
    //保存每一列的宽度的值
    protected Map<String, Integer> columnWidthMap = new HashMap<>();

    public ExcelUtilAbstract(final OutputStream outputStream) throws IOException {
        super(outputStream);
    }

    public ExcelUtilAbstract(String filePath) throws IOException {
        super(filePath);
    }

    public ExcelUtilAbstract(URL fileURL) throws IOException {
        super(fileURL);
    }

    public ExcelUtilAbstract(File file) throws IOException {
        super(file);
    }

    /**
     * 获取excel文档的类型
     *
     * @return excel文档的类型
     */
    public ExcelDocumentEnum getExcelDocumentEnum() {
        return excelDocumentEnum;
    }

    /**
     * 获取是否忽略读写过程中的错误数据，
     *
     * @return 是否忽略错误继续
     */
    public boolean isIgnoreError() {
        return ignoreError;
    }

    /**
     * 设置是否忽略错误继续
     *
     * @param ignoreError 是否忽略错误继续
     */
    public void setIgnoreError(boolean ignoreError) {
        this.ignoreError = ignoreError;
    }

    public boolean isIgnoreEmptyLine() {
        return ignoreEmptyLine;
    }

    public void setIgnoreEmptyLine(boolean ignoreEmptyLine) {
        this.ignoreEmptyLine = ignoreEmptyLine;
    }

    /**
     * 设置列的数据保存格式
     *
     * @param columnName 列的名称
     * @param format     列的保存格式
     */
    public void setColumnFormat(String columnName, String format) {
        columnTypeMap.put(columnName, format);
    }

    /**
     * 设置列的数据保存格式
     *
     * @param columnName 列的名称
     * @param width      宽度
     */
    public void setCellWidth(String columnName, int width) {
        columnWidthMap.put(columnName, Math.min(width, 254));
    }

    /**
     * 获取文档的sheet数量
     *
     * @return sheet数量
     */
    public abstract int getNumberOfSheets() throws IOException;

    /**
     * 获取所有工作薄中的就条数
     *
     * @return 记录数
     * @throws IOException 读写异常
     */
    public abstract List<Integer> getRowsOfSheets() throws IOException;

    /**
     * 将excel文件中的数据读入到到datatable中
     */
    public abstract List<DataTable> read() throws IOException;

    /**
     * 将excel文件中的数据读入到到datatable中
     *
     * @param sheetIndex sheet页序号
     */
    public abstract DataTable read(int sheetIndex) throws IOException;

    /**
     * 将excel文件中的数据读入到到datatable中
     *
     * @param sheetName sheet名称
     */
    public abstract DataTable read(String sheetName) throws IOException;

    /**
     * 将excel中的数据读取并采用自定义程序进行处理
     *
     * @param xlsxProcess 自定义的数据处理类
     */
    public final void process(IXlsxProcess xlsxProcess) throws Exception {
        process(xlsxProcess, 0);
    }

    /**
     * 将excel中的数据读取并采用自定义程序进行处理
     *
     * @param xlsxProcess 自定义的数据处理类
     * @param sheetIndex  sheet页序号
     */
    public abstract void process(IXlsxProcess xlsxProcess, int sheetIndex) throws Exception;

    /**
     * 将excel中的数据读取并采用自定义程序进行处理
     *
     * @param xlsxProcess 自定义的数据处理类
     * @param sheetName   sheet名称
     */
    public abstract void process(IXlsxProcess xlsxProcess, String sheetName) throws Exception;

    protected void read(List<DataTable> dataTables, Workbook workbook) throws DataGetException {
        for (int i = 0; ; i++) {
            DataTable dataTable = new DataTable();
            String sheetName = new XlsReader().readFile(workbook, i, null, new DataTableXlsxProcess(dataTable, isIgnoreEmptyLine()), isIgnoreError());
            if (sheetName != null) {
                dataTable.setTableName(sheetName);
                dataTables.add(dataTable);
            } else {
                break;
            }
        }
    }

    @Override
    public void write(DataTable dataTable) throws IOException {
        List<DataTable> dataTables = new ArrayList<>(1);
        dataTables.add(dataTable);
        write(dataTables);
    }

    /**
     * 写入数据到excel文件
     *
     * @param dataTables 需要写入的数据表格集合
     * @throws IOException 写入异常
     */
    public abstract void write(List<DataTable> dataTables) throws IOException;

    /**
     * 将数据写入到文件中的方法
     *
     * @param dataTable 需要写入excel的数据
     * @return 错误的内容
     * @throws IOException 无法写入到磁盘的错误
     */
    protected String writeOutputStream(DataTable dataTable) throws IOException {
        Sheet sheet = workbook.createSheet(dataTable.getTableName());
        //添加数据
        int index = 0;

        DataFormat format = workbook.createDataFormat();
        CellStyle defaultCellStyle = workbook.createCellStyle();
        defaultCellStyle.setDataFormat(format.getFormat("@"));

        //写入标题
        Row row = sheet.createRow(index);
        for (int i = 0; i < dataTable.columnCount(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(defaultCellStyle);
            cell.setCellValue(dataTable.columnNames()[i]);
            if (columnWidthMap.containsKey(dataTable.columnNames()[i])) {
                sheet.setColumnWidth(i, 256 * columnWidthMap.get(dataTable.columnNames()[i]) + 184);
            }
        }

        //设置每一列的样式
        CellStyle[] dataStyles = new CellStyle[dataTable.columnCount()];
        for (int i = 0; i < dataTable.columnCount(); i++) {
            String columnName = dataTable.columnNames()[i];
            if (columnTypeMap.containsKey(columnName)) {
                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setDataFormat(format.getFormat(columnTypeMap.get(columnName)));
                dataStyles[i] = cellStyle;
            }
        }
        StringBuilderExt errorBuilder = new StringBuilderExt();

        //遍历并写入每一行的数据
        for (DataRow dataRow : dataTable.dataRows()) {
            index++;
            row = sheet.createRow(index);
            for (int i = 0; i < dataTable.columnCount(); i++) {
                Object object = dataRow.getValues()[i];
                if (object != null) {
                    Cell cell = row.createCell(i);
                    if (dataStyles[i] != null) {
                        cell.setCellStyle(dataStyles[i]);
                    }

                    if (object instanceof Number) {
                        cell.setCellValue(Double.valueOf(object.toString()));
                    } else if (object instanceof String) {
                        cell.setCellValue((String) object);
                    } else if (object instanceof Date) {
                        cell.setCellValue((Date) object);
                    } else if (object instanceof Calendar) {
                        cell.setCellValue((Calendar) object);
                    } else if (object instanceof RichTextString) {
                        cell.setCellValue((RichTextString) object);
                    } else {
                        cell.setCellValue(object.toString());
                    }
                }
            }
        }

        return errorBuilder.toString();
    }

    protected void writeFile(List<DataTable> dataTables) throws IOException {
        for (int i = 0; i < dataTables.size(); i++) {
            if (StringUtils.isBlank(dataTables.get(i).getTableName())) {
                dataTables.get(i).setTableName("Sheet" + i);
            }
        }

        try {
            String error = StringUtils.EMPTY;
            for (DataTable dataTable : dataTables) {
                error += writeOutputStream(dataTable);
            }
            if (StringUtils.isBlank(error)) {
                workbook.write(getOutputStream());
            } else {
                //如果错误内容不为空
                if (ignoreError) {
                    LOGGER.warn(error);
                    workbook.write(getOutputStream());
                } else {
                    throw new IOException(error);
                }
            }
        } finally {
            close();
        }
    }

    @Override
    public void close() throws IOException {
        if (workbook != null) {
            workbook.close();
        }
        super.close();
    }

    /**
     * 将double对象转换为日期对象
     *
     * @param number 需要转换的日期对象
     * @return 日期的值
     */
    public static Date toDate(double number) {
        return DateUtil.getJavaDate(number);
    }

    /**
     * 尝试将object对象转换为日期对象，如果转换失败，返回null值
     *
     * @param value 需要转换的日期对象
     * @return 日期的值
     */
    public static Date tryToDate(Object value) {
        if (value == null) {
            return null;
        }

        //如果是数字类型，那么采用excel的方法来转换
        if (value instanceof Number) {
            return DateUtil.getJavaDate(Double.parseDouble(value.toString()));
        } else if (value instanceof Date) {
            return (Date) value;
        } else {
            //否则按照日期行文本内容进行转换
            return DateUtils.tryToDate(value.toString());
        }
    }
}
