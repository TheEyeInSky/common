package com.dongzy.common.common.io.excel;

import org.xml.sax.SAXException;
import com.gee4j.common.DataGetException;

import java.sql.SQLException;

/**
 * Excel单行数据处理程序
 */
public interface IXlsxProcess {

    /**
     * 处理当行的数据
     *
     * @param rowIndex 当前的行号
     * @param values   行里面的数据集合
     */
    void process(int rowIndex, Object[] values) throws DataGetException;

    /**
     * 开始读取文档时触发的事件
     *
     * @throws SAXException 解析XML异常
     */
    void startDocument() throws SAXException;

    /**
     * 结束文档读取时触发的事件
     *
     * @throws SAXException 解析XML异常
     */
    void endDocument() throws SAXException;

    /**
     * 处理第一行数据
     *
     * @param values 第一行数据
     */
    void processFirstRow(Object[] values) throws DataGetException;

    /**
     * 处理第一行之外的数据
     *
     * @param values 第一行之外的数据
     */
    void processOtherRow(Object[] values) throws DataGetException;
}
