package com.dongzy.common.common.io.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.gee4j.common.Validate;
import com.gee4j.common.io.PathUtils;
import com.gee4j.data.table.DataTable;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于读写Excel的工具类，建议处理不超过10M的文件
 * 如果处理更大的文件，应该采用BigExcelUtil工具来进行处理。
 */
public class ExcelUtils extends ExcelUtilAbstract {

    public ExcelUtils(final OutputStream outputStream, ExcelDocumentEnum excelDocumentEnum) throws IOException {
        super(outputStream);
        this.excelDocumentEnum = excelDocumentEnum;
    }

    public ExcelUtils(String filePath) throws IOException {
        super(filePath);
        initType(filePath);
    }

    public ExcelUtils(URL fileURL) throws IOException {
        super(fileURL);
        initType(PathUtils.fromUrl(fileURL).toString());
    }

    public ExcelUtils(File file) throws IOException {
        super(file);
        initType(file.getAbsolutePath());
    }

    @Override
    public int getNumberOfSheets() throws IOException {
        return 0;
    }

    @Override
    public List<Integer> getRowsOfSheets() throws IOException {
        return null;
    }

    private void initType(String filePath) {
        String extName = PathUtils.getFileExtensionName(filePath).toLowerCase();
        switch (extName) {
            case "xls":
                excelDocumentEnum = ExcelDocumentEnum.XLS;
                break;
            case "xlsx":
                excelDocumentEnum = ExcelDocumentEnum.XLSX;
                break;
            default:
                throw new IllegalArgumentException("文件不是合法的excel文件！");
        }
    }

    @Override
    public List<DataTable> read() throws IOException {
        List<DataTable> dataTables = new ArrayList<>();
        try (Workbook workbook = getWorkbook()) {
            read(dataTables, workbook);
        }
        return dataTables;
    }

    @Override
    synchronized public DataTable read(int sheetIndex) throws IOException {
        Validate.isTrue(sheetIndex >= 0, "sheetIndex必须大于等于0！");
        DataTable dataTable = new DataTable();
        try (Workbook workbook = getWorkbook()) {
            String sheetName = new XlsReader().readFile(workbook, sheetIndex, null,
                    new DataTableXlsxProcess(dataTable, isIgnoreEmptyLine()), isIgnoreError());
            if (sheetName == null) {
                throw new IndexOutOfBoundsException("sheetIndex：[" + sheetIndex + "] 越界");
            } else {
                dataTable.setTableName(sheetName);
            }
        }
        return dataTable;
    }

    @Override
    public DataTable read(String sheetName) throws IOException {
        Validate.notBlank(sheetName, "获取数据的sheetName不能为空！");
        DataTable dataTable = new DataTable();
        try (Workbook workbook = getWorkbook()) {
            String name = new XlsReader().readFile(workbook, -1, sheetName,
                    new DataTableXlsxProcess(dataTable, isIgnoreEmptyLine()), isIgnoreError());
            if (name == null) {
                throw new IndexOutOfBoundsException("sheetName：[" + sheetName + "] 不存在");
            } else {
                dataTable.setTableName(name);
            }
        }
        return dataTable;
    }

    @Override
    public void process(IXlsxProcess xlsxProcess, int sheetIndex) throws Exception {
        try (Workbook workbook = getWorkbook()) {
            new XlsReader().readFile(workbook, sheetIndex, null, xlsxProcess, isIgnoreError());
        }
    }

    @Override
    public void process(IXlsxProcess xlsxProcess, String sheetName) throws Exception {
        try (Workbook workbook = getWorkbook()) {
            new XlsReader().readFile(workbook, -1, sheetName, xlsxProcess, isIgnoreError());
        }
    }

    /**
     * 写入文本的内容
     *
     * @param dataTables 需要保存的内容
     * @throws IOException 读取excel文件异常
     */
    @Override
    synchronized public void write(List<DataTable> dataTables) throws IOException {
        switch (excelDocumentEnum) {
            case XLS:
                workbook = new HSSFWorkbook();
                break;
            case XLSX:
                workbook = new XSSFWorkbook();
                break;
            default:
                throw new IllegalArgumentException("文件不是合法的excel文件！");
        }

        writeFile(dataTables);
    }

    /**
     * 获取WorkBook对象
     *
     * @return WorkBook对象
     */
    private Workbook getWorkbook() throws IOException {
        switch (excelDocumentEnum) {
            case XLS:
                return new HSSFWorkbook(new POIFSFileSystem(getFile(), true));
            case XLSX:
                try {
                    return new XSSFWorkbook(OPCPackage.open(getFile()));
                } catch (InvalidFormatException e) {
                    throw new IOException("读取excel内容格式异常", e);
                }
            default:
                throw new IllegalArgumentException("文件不是合法的excel文件！");
        }
    }
}
