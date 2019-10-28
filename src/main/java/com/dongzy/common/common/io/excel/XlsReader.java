package com.dongzy.common.common.io.excel;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import com.gee4j.common.DataGetException;
import com.gee4j.common.Validate;
import com.gee4j.common.text.StringBuilderExt;
import com.gee4j.common.text.StringUtils;
import com.gee4j.log.TextLoggerFactory;

import java.io.IOException;

/**
 * 用于读写Excel的辅助类，只能处理标准xls文件或者体积较小的xlsx文件
 * 建议xlsx文件的记录条数小于65536条，列数少于256列
 * 如果处理更大的文件，应该采用BigExcelUtil工具来进行处理。
 */
public class XlsReader {

    private static final Logger LOGGER = TextLoggerFactory.getInstance().getLogger(XlsReader.class);
    private FormulaEvaluator evaluator;

    /**
     * 读取2003格式的excel文件
     *
     * @param sheetName   sheet页面的名称
     * @param sheetIndex  sheetIndex的序号
     * @param workbook    workBook对象
     * @param xlsxProcess 行数据处理程序
     * @param ignoreError 遇到单元格读取错误是抛出异常，还是忽略错误继续
     * @throws IOException 文件异常信息
     */
    public String readFile(Workbook workbook, int sheetIndex, String sheetName, IXlsxProcess xlsxProcess, boolean ignoreError) throws DataGetException {

        Validate.isTrue(!(sheetIndex == -1 && StringUtils.isBlank(sheetName)), "SheetName is null");
        Validate.isTrue(!(sheetIndex >= 0 && StringUtils.notBlank(sheetName)), "SheetIndex and SheetName only one");

        //创建公式计算类
        evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        Sheet sheet;
        if (sheetIndex >= 0) {
            if (sheetIndex < workbook.getNumberOfSheets()) {
                sheet = workbook.getSheetAt(sheetIndex);
            } else {
                return null;
            }
        } else {
            sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                return null;
            }
        }

        //读取标题行，由标题行确定数据的总列数
        int index = sheet.getFirstRowNum();
        Row row = sheet.getRow(index);
        int startColumnIndex = row.getFirstCellNum();                   //开始的列序号
        int endColumnIndex = row.getLastCellNum();                      //结束的列序号
        int columnCount = endColumnIndex - startColumnIndex;            //标题行的总列数
        int rowCount = sheet.getLastRowNum();

        StringBuilderExt errorBuilder = new StringBuilderExt();

        for (; index <= rowCount; index++) {
            row = sheet.getRow(index);
            if (row != null) {
                Object[] objects = new Object[columnCount];
                for (int col = startColumnIndex; col < endColumnIndex; col++) {
                    try {
                        objects[col] = getCellValue(row.getCell(col));
                    } catch (Throwable th) {
                        errorBuilder.appendFormatLine("{0} 行 {1} 列 {2} 读取失败", sheet.getSheetName(), index + 1, col + 1);
                    }
                }
                xlsxProcess.process(index, objects);
            }
        }

        String error = errorBuilder.toString();
        if (StringUtils.notBlank(error)) {
            if (ignoreError) {
                LOGGER.warn(error);
            } else {
                throw new DataGetException(error);
            }
        }

        return sheet.getSheetName();
    }

    //获取单元格式的数据
    private Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case NUMERIC:
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case ERROR:
                return cell.getErrorCellValue();
            case FORMULA:
                return getCellForumlaValue(cell);
            case BLANK:
                return StringUtils.EMPTY;
            case _NONE:
            default:
                return null;
        }
    }

    //获取计算单元个的数据
    private Object getCellForumlaValue(Cell cell) {
        try {
            CellValue cellValue = evaluator.evaluate(cell);
            switch (cellValue.getCellType()) {
                case NUMERIC:
                    return cellValue.getNumberValue();
                case STRING:
                    return cellValue.getStringValue();
                case BOOLEAN:
                    return cellValue.getBooleanValue();
                case ERROR:
                    return cellValue.getErrorValue();
                case BLANK:
                    return StringUtils.EMPTY;
                case FORMULA:
                case _NONE:
                default:
                    return null;
            }
        } catch (NotImplementedException e) {
            return null;
        }
    }
}
