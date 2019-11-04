package com.dongzy.common.common.io.excel;


import com.dongzy.common.common.io.PathUtils;
import com.dongzy.common.data.table.DataTable;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于读写大Excel文件的工具类（超过10M以上）
 * 大文件的一般定义为超过10M以上的文件
 * 大文件如果采用传入的方式读取，很容易导致内存溢出和速度很慢的问题
 * 但是大文件读取的弊端就是会丢失单元格的展示相关信息
 * 所以采用哪个组件读取，是需要综合考虑的。
 */
public class BigExcelUtils extends ExcelUtilAbstract {

    public BigExcelUtils(final OutputStream outputStream, ExcelDocumentEnum excelDocumentEnum) throws IOException {
        super(outputStream);
        this.excelDocumentEnum = excelDocumentEnum;
    }

    public BigExcelUtils(String filePath) throws IOException {
        super(filePath);
        initType(filePath);
    }

    public BigExcelUtils(URL fileURL) throws IOException {
        super(fileURL);
        initType(PathUtils.fromUrl(fileURL).toString());
    }

    public BigExcelUtils(File file) throws IOException {
        super(file);
        initType(file.getAbsolutePath());
    }

    //判断需要读取的文件类型，这里分类xls和xlsx两种
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
    public void process(IXlsxProcess xlsxProcess, int sheetIndex) throws Exception {
        switch (excelDocumentEnum) {
            case XLS:
                try (Workbook workbook = new HSSFWorkbook(new POIFSFileSystem(getFile(), true))) {
                    new XlsReader().readFile(workbook, sheetIndex, null, xlsxProcess, isIgnoreError());
                }
                break;
            case XLSX:
                try (OPCPackage pkg = OPCPackage.open(getFile())) {
                    new XlsxReader().readFile(pkg, sheetIndex, null, xlsxProcess, isIgnoreError());
                }
                break;
        }
    }

    @Override
    public void process(IXlsxProcess xlsxProcess, String sheetName) throws Exception {
        switch (excelDocumentEnum) {
            case XLS:
                try (Workbook workbook = new HSSFWorkbook(new POIFSFileSystem(getFile(), true))) {
                    new XlsReader().readFile(workbook, -1, sheetName, xlsxProcess, isIgnoreError());
                }
                break;
            case XLSX:
                try (OPCPackage pkg = OPCPackage.open(getFile())) {
                    new XlsxReader().readFile(pkg, -1, sheetName, xlsxProcess, isIgnoreError());
                }
                break;
        }
    }

    @Override
    public int getNumberOfSheets() throws IOException {
        switch (excelDocumentEnum) {
            case XLS:
                try (Workbook workbook = new HSSFWorkbook(new POIFSFileSystem(getFile(), true))) {
                    return workbook.getNumberOfSheets();
                }
            case XLSX:
                try (OPCPackage pkg = OPCPackage.open(getFile())) {
                    XSSFReader xssfReader = new XSSFReader(pkg);
                    int i = 0;
                    XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
                    while (iter.hasNext()) {
                        try (InputStream ignored = iter.next()) {
                            i++;
                        }
                    }
                    return i;
                } catch (OpenXML4JException e) {
                    throw new IOException("无法正确解析excel的xml文件内容。", e);
                }
            default:
                throw new IllegalArgumentException("未知的类型!");
        }
    }

    @Override
    public List<Integer> getRowsOfSheets() throws IOException {
        List<Integer> rows = new ArrayList<>();
        switch (excelDocumentEnum) {
            case XLS:
                try (Workbook workbook = new HSSFWorkbook(new POIFSFileSystem(getFile(), true))) {
                    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                        rows.add(workbook.getSheetAt(i).getLastRowNum() + 1);
                    }
                }
                break;
            case XLSX:
                try (OPCPackage pkg = OPCPackage.open(getFile())) {
                    XSSFReader xssfReader = new XSSFReader(pkg);
                    XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
                    while (iter.hasNext()) {
                        try (InputStream ignored = iter.next()) {
                            rows.add(-1);
                        }
                    }
                } catch (OpenXML4JException e) {
                    throw new IOException("无法正确解析excel的xml文件内容。", e);
                }
                break;
        }
        return rows;
    }

    @Override
    public List<DataTable> read() throws IOException {
        List<DataTable> dataTables = new ArrayList<>();
        switch (excelDocumentEnum) {
            case XLS:
                try (Workbook workbook = new HSSFWorkbook(new POIFSFileSystem(getFile(), true))) {
                    read(dataTables, workbook);
                }
                break;
            case XLSX:
                try (OPCPackage pkg = OPCPackage.open(getFile())) {
                    for (int i = 0; ; i++) {
                        final DataTable dataTable = new DataTable();
                        //设置为接收大数据配置
                        dataTable.setBiggerData(true);
                        String sheetName = new XlsxReader().readFile(pkg, i, null, new DataTableXlsxProcess(dataTable, isIgnoreEmptyLine()), isIgnoreError());
                        if (sheetName != null) {
                            dataTable.setTableName(sheetName);
                            dataTables.add(dataTable);
                        } else {
                            break;
                        }
                    }
                } catch (SAXException | ParserConfigurationException | OpenXML4JException e) {
                    throw new IOException("无法正确解析excel的xml文件内容。", e);
                }
                break;
        }
        return dataTables;
    }

    /**
     * 读取excel文件
     *
     * @return 表格对象
     */
    @Override
    synchronized public DataTable read(int sheetIndex) throws IOException {
        final DataTable dataTable = new DataTable();
        //设置为接收大数据配置
        dataTable.setBiggerData(true);
        switch (excelDocumentEnum) {
            case XLS:
                try (Workbook workbook = new HSSFWorkbook(new POIFSFileSystem(getFile(), true))) {
                    String sheetName = new XlsReader().readFile(workbook, sheetIndex, null, new DataTableXlsxProcess(dataTable, isIgnoreEmptyLine()), isIgnoreError());
                    if (sheetName == null) {
                        throw new IndexOutOfBoundsException("sheetIndex：[" + sheetIndex + "] 越界");
                    } else {
                        dataTable.setTableName(sheetName);
                    }
                }
                break;
            case XLSX:
                try (OPCPackage pkg = OPCPackage.open(getFile())) {
                    String sheetName = new XlsxReader().readFile(pkg, sheetIndex, null, new DataTableXlsxProcess(dataTable, isIgnoreEmptyLine()), isIgnoreError());
                    if (sheetName == null) {
                        throw new IndexOutOfBoundsException("sheetIndex：[" + sheetIndex + "] 越界");
                    } else {
                        dataTable.setTableName(sheetName);
                    }
                } catch (SAXException | ParserConfigurationException | OpenXML4JException e) {
                    throw new IOException("无法正确解析excel的xml文件内容。", e);
                }
                break;
        }

        return dataTable;
    }

    @Override
    public DataTable read(String sheetName) throws IOException {
        final DataTable dataTable = new DataTable();
        //设置为接收大数据配置
        dataTable.setBiggerData(true);
        switch (excelDocumentEnum) {
            case XLS:
                try (Workbook workbook = new HSSFWorkbook(new POIFSFileSystem(getFile(), true))) {
                    String name = new XlsReader().readFile(workbook, -1, sheetName, new DataTableXlsxProcess(dataTable, isIgnoreEmptyLine()), isIgnoreError());
                    if (name == null) {
                        throw new IndexOutOfBoundsException("sheetName：[" + sheetName + "] 不存在");
                    } else {
                        dataTable.setTableName(name);
                    }
                }
                break;
            case XLSX:
                try (OPCPackage pkg = OPCPackage.open(getFile())) {
                    String name = new XlsxReader().readFile(pkg, -1, sheetName, new DataTableXlsxProcess(dataTable, isIgnoreEmptyLine()), isIgnoreError());
                    if (name == null) {
                        throw new IndexOutOfBoundsException("sheetName：[" + sheetName + "] 不存在");
                    } else {
                        dataTable.setTableName(name);
                    }
                } catch (SAXException | ParserConfigurationException | OpenXML4JException e) {
                    throw new IOException("无法正确解析excel的xml文件内容。", e);
                }
                break;
        }

        return dataTable;
    }

    /**
     * 写入文本的内容
     *
     * @param dataTables 需要保存的内容
     * @throws IOException 写入文件时异常
     */
    @Override
    synchronized public void write(List<DataTable> dataTables) throws IOException {
        switch (excelDocumentEnum) {
            case XLS:
                workbook = new HSSFWorkbook();
                break;
            case XLSX:
                workbook = new SXSSFWorkbook(100);
                break;
        }

        writeFile(dataTables);
    }

    @Override
    public void close() throws IOException {
        if (workbook != null) {
            workbook.close();
        }
        super.close();
    }
}
