package com.dongzy.common.common.io.excel;

/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.slf4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import com.gee4j.common.DataGetException;
import com.gee4j.common.Validate;
import com.gee4j.common.text.StringBuilderExt;
import com.gee4j.common.text.StringUtils;
import com.gee4j.log.TextLoggerFactory;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * 采用传入的方式在读取大文件时，容易出现内存溢出的问题，本类用于解析大型的文件（5M以上的文件）
 *
 * @author zouyong
 */
public class XlsxReader {

    private static final Logger LOGGER = TextLoggerFactory.getInstance().getLogger(XlsxReader.class);

    /**
     * The type of the data value is indicated by an attribute on the cell. The
     * value is usually in a "v" element within the cell.
     */
    enum XssfDataType {
        BOOLEAN, ERROR, FORMULA, INLINESTR, SSTINDEX, NUMBER
    }

    /**
     * 使用xssf_sax_API处理Excel,请参考： http://poi.apache.org/spreadsheet/how-to.html#xssf_sax_api
     * <p/>
     * Also see Standard ECMA-376, 1st edition, part 4, pages 1928ff, at
     * http://www.ecma-international.org/publications/standards/Ecma-376.htm
     * <p/>
     * A web-friendly version is http://openiso.org/Ecma/376/Part4
     */
    class MyXSSFSheetHandler extends DefaultHandler {

        private final static String ERROR_CELL_CONTENT = "545_EXCEL_CELL_ERROR_65482145";

        /**
         * Table with styles
         */
        private StylesTable stylesTable;

        /**
         * Table with unique strings
         */
        private ReadOnlySharedStringsTable sharedStringsTable;

        // Set when V start element is seen
        private boolean vIsOpen;

        // Set when cell start element is seen;
        // used when cell close element is seen.
        private XssfDataType nextDataType;

        // Used to format numeric cell values.
        private short formatIndex;
        private String formatString;
        private final DataFormatter formatter;

        private int thisColumn = -1;
        // The last column printed to the output stream
        private int lastColumnNumber = -1;

        // Gathers characters as they are seen.
        private StringBuffer value;
        private Object[] record;
        private boolean cellNull = false;
        private int currentRow = 0;         //当前的行数

        /**
         * Accepts objects needed while parsing.
         *
         * @param styles  Table of styles
         * @param strings Table of shared strings
         */
        public MyXSSFSheetHandler(StylesTable styles, ReadOnlySharedStringsTable strings) {
            this.stylesTable = styles;
            this.sharedStringsTable = strings;
            this.value = new StringBuffer();
            this.nextDataType = XssfDataType.NUMBER;
            this.formatter = new DataFormatter();
            record = new Object[columnCount];
        }

        @Override
        public void startDocument() throws SAXException {
            xlsxProcess.startDocument();
        }

        @Override
        public void endDocument() throws SAXException {
            xlsxProcess.endDocument();
        }

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {

            //下面的三类节点被认为是数据节点
            if ("inlineStr".equals(name) || "v".equals(name) || "t".equals(name)) {
                vIsOpen = true;
                // Clear contents cache
                value.setLength(0);
            } else if ("c".equals(name)) {        //c => cell节点表示单元格节点
                // Get the cell reference
                String r = attributes.getValue("r");
                int firstDigit = -1;
                for (int c = 0; c < r.length(); ++c) {
                    if (Character.isDigit(r.charAt(c))) {
                        firstDigit = c;
                        break;
                    }
                }
                thisColumn = nameToColumn(r.substring(0, firstDigit));

                // Set up defaults.
                this.nextDataType = XssfDataType.NUMBER;
                this.formatIndex = -1;
                this.formatString = null;
                String cellType = attributes.getValue("t");
                String cellStyleStr = attributes.getValue("s");
                if ("b".equals(cellType))
                    nextDataType = XssfDataType.BOOLEAN;
                else if ("e".equals(cellType))
                    nextDataType = XssfDataType.ERROR;
                else if ("inlineStr".equals(cellType))
                    nextDataType = XssfDataType.INLINESTR;
                else if ("s".equals(cellType))
                    nextDataType = XssfDataType.SSTINDEX;
                else if ("str".equals(cellType))
                    nextDataType = XssfDataType.FORMULA;
                else if (cellStyleStr != null) {
                    int styleIndex = Integer.parseInt(cellStyleStr);
                    XSSFCellStyle style = stylesTable.getStyleAt(styleIndex);
                    this.formatIndex = style.getDataFormat();
                    this.formatString = style.getDataFormatString();
                    if (this.formatString == null) {
                        this.formatString = BuiltinFormats.getBuiltinFormat(this.formatIndex);
                    }
                }
            }

        }

        /*
         * (non-Javadoc)
         *
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
         * java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String name) {

            Object cellValue;

            //下面的两类节点被认为是数据节点结束符
            if ("v".equals(name) || "t".equals(name)) {

                //超出列的数据范围
                if (thisColumn >= columnCount) {
                    return;
                }
                // Process the value contents as required.
                // Do now, as characters() may be called more than once
                switch (nextDataType) {
                    case BOOLEAN:
                        char first = value.charAt(0);
                        cellValue = (first != '0');
                        break;
                    case ERROR:
                        cellValue = ERROR_CELL_CONTENT;
                        break;
                    case FORMULA:
                        cellValue = value;
                        break;
                    case INLINESTR:
                        XSSFRichTextString rtsi = new XSSFRichTextString(value.toString());
                        cellValue = rtsi.toString();
                        break;
                    case SSTINDEX:
                        String sstIndex = value.toString();
                        try {
                            int idx = Integer.parseInt(sstIndex);
                            RichTextString rtss = sharedStringsTable.getItemAt(idx);
                            cellValue = rtss.toString();
                        } catch (NumberFormatException ex) {
                            cellValue = "Failed to parse SST index '" + sstIndex + "': " + ex.toString();
                        }
                        break;
                    case NUMBER:
                        String str = value.toString();
                        // 判断是否是日期格式
                        if (HSSFDateUtil.isADateFormat(this.formatIndex, str)) {
                            Double d = Double.parseDouble(str);
                            cellValue = HSSFDateUtil.getJavaDate(d);
                        } else {
                            cellValue = Double.parseDouble(str);
                        }
                        break;
                    default:
                        cellValue = "(TODO: Unexpected type: " + nextDataType + ")";
                        break;
                }

                // Output after we've seen the string contents
                // Emit commas for any fields that were missing on this row
                if (lastColumnNumber == -1) {
                    lastColumnNumber = 0;
                }
                //判断单元格的值是否为空
                if (cellValue == null) {
                    cellNull = true;// 设置单元格是否为空值
                }
                record[thisColumn] = cellValue;
                // Update column
                if (thisColumn > -1) {
                    lastColumnNumber = thisColumn;
                }

            } else if ("row".equals(name)) {

                // Print out any missing commas if needed
                if (columnCount > 0) {
                    // Columns are 0 based
                    if (lastColumnNumber == -1) {
                        lastColumnNumber = 0;
                    }
                    if (!cellNull) {// 判断是否空行

                        //因为第一行为标题行，所以第一行重新定义了整个表格数据的宽度
                        if (currentRow == 0) {
                            int columnIndex = record.length;
                            for (int i = columnIndex - 1; i >= 0; i--) {
                                if (record[i] != null) {
                                    columnIndex = i;
                                    break;
                                }
                            }
                            columnCount = columnIndex + 1;
                            record = Arrays.copyOf(record, columnCount);
                        }

                        //处理本行数据
                        for (int i = 0; i < record.length; i++) {
                            if (ERROR_CELL_CONTENT.equals(record[i])) {
                                errorBuilder.appendFormatLine("{0} 行 {1} 列 {2} 读取失败", currentSheetName, currentRow, i + 1);
                                record[i] = StringUtils.EMPTY;
                            }
                        }
                        try {
                            xlsxProcess.process(currentRow, record);
                        } catch (DataGetException e) {
                            errorBuilder.appendFormatLine(e.getMessage());
                        }
                        currentRow++;
                        cellNull = false;
                        for (int i = 0; i < record.length; i++) {
                            record[i] = null;
                        }
                    }
                }
                lastColumnNumber = -1;
            }

        }

        /**
         * Captures characters only if a suitable element is open. Originally
         * was just "v"; extended for inlineStr also.
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (vIsOpen) {
                value.append(ch, start, length);
            }
        }

        /**
         * 将excel中的列名转换为数字的列序号
         *
         * @param name 列名：如A、B、C、AC等
         * @return 名字对应的需要，如A对应0，B对应1等。
         */
        private int nameToColumn(String name) {
            int column = -1;
            for (int i = 0; i < name.length(); ++i) {
                int c = name.charAt(i);
                if (c > 'Z' || c < 'A') {       //如果列名中存在非字母内容，那么直接忽略掉
                    continue;
                }
                column = (column + 1) * 26 + c - 'A';
            }
            return column;
        }
    }

    // /////////////////////////////////////

    private OPCPackage xlsxPackage;
    /**
     * 表格中数据的列数，初始化为10000列，超过10000列的数据将会被忽略
     */
    private int columnCount = 10000;
    private IXlsxProcess xlsxProcess;
    /**
     * 是否忽略读取过程中遇到的错误
     */
    private boolean ignoreError;
    /**
     * 记录错误信息
     */
    private StringBuilderExt errorBuilder = new StringBuilderExt();
    /**
     * 当前读取的sheet页面的名称
     */
    private String currentSheetName;

    /**
     * 读取Excel
     *
     * @param pkg         OPCPackage实例
     * @param sheetIndex  sheet的序列号
     * @param sheetName   sheet的名称
     * @param xlsxProcess 行数据处理程序
     * @param ignoreError 遇到单元格读取错误是抛出异常，还是忽略错误继续
     */
    public String readFile(OPCPackage pkg, int sheetIndex, String sheetName, IXlsxProcess xlsxProcess, boolean ignoreError)
            throws IOException, OpenXML4JException, ParserConfigurationException, SAXException {

        this.ignoreError = ignoreError;

        Validate.isTrue(!(sheetIndex == -1 && StringUtils.isBlank(sheetName)), "SheetName is null");
        Validate.isTrue(!(sheetIndex >= 0 && StringUtils.notBlank(sheetName)), "SheetIndex and SheetName only one");

        this.xlsxPackage = pkg;
        this.xlsxProcess = xlsxProcess;

        ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(xlsxPackage);
        XSSFReader xssfReader = new XSSFReader(xlsxPackage);

        StylesTable styles = xssfReader.getStylesTable();
        XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();

        int index = 0;
        while (iter.hasNext()) {
            try (InputStream stream = iter.next()) {
                if (sheetIndex == index || StringUtils.equalsIgnoreCase(sheetName, iter.getSheetName())) {
                    currentSheetName = iter.getSheetName();
                    processSheet(styles, strings, stream);
                    return currentSheetName;
                }
            }
            index++;
        }

        //未找到任何sheet，返回null
        return null;
    }

    /**
     * Parses and shows the content of one sheet using the specified styles and
     * shared-strings tables.
     *
     * @param styles           style
     * @param strings          string
     * @param sheetInputStream inputstream
     */
    private void processSheet(StylesTable styles, ReadOnlySharedStringsTable strings, InputStream sheetInputStream)
            throws IOException, ParserConfigurationException, SAXException {

        InputSource sheetSource = new InputSource(sheetInputStream);
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxFactory.newSAXParser();
        XMLReader sheetParser = saxParser.getXMLReader();
        MyXSSFSheetHandler handler = new MyXSSFSheetHandler(styles, strings);
        sheetParser.setContentHandler(handler);
        sheetParser.parse(sheetSource);

        String error = errorBuilder.toString();
        if (StringUtils.notBlank(error)) {
            if (ignoreError) {
                LOGGER.warn(error);
            } else {
                throw new DataGetException(error);
            }
        }
    }
}