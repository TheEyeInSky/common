package com.dongzy.common.common.io.excel;

import com.dongzy.common.common.DataGetException;
import com.dongzy.common.common.collection.ArrayUtils;
import com.dongzy.common.common.text.StringUtils;
import com.dongzy.common.log.TextLoggerFactory;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

/**
 * 抽象excel处理类
 */
public abstract class AbstractXlsxProcess implements IXlsxProcess {

    private final static Logger LOGGER = TextLoggerFactory.getInstance().getLogger(IXlsxProcess.class);
    protected boolean ignoreEmptyLine;
    protected boolean firstRow = true;       //标记是否为第一行数据

    @Override
    public void process(int rowIndex, Object[] values) throws DataGetException {
        //如果是第一行，并且是空行，那么就直接跳过去
        if (firstRow && isEmptyLine(values)) {
            return;
        }

        if (firstRow) {        //如是第一行，那么创建为表格的标题
            processFirstRow(values);
            firstRow = false;         //标记第一行已经处理了
        } else {
            //如果不是第一行，那么就是数据
            //如果不忽略空行，或者内容不是全空，那么添加该行
            if (!ignoreEmptyLine || !isEmptyLine(values)) {
                processOtherRow(values);
            }
        }

        if (rowIndex != 0 && rowIndex % 5000 == 0) {
            LOGGER.debug("Read " + rowIndex + " lines.");
        }
    }

    @Override
    public void startDocument() throws SAXException {
        LOGGER.debug("start document.");
    }

    @Override
    public void endDocument() throws SAXException {
        LOGGER.debug("end document.");
    }

    protected boolean isEmptyLine(Object[] values) {
        Object[] result = new Object[values.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = values[i];
            if (values[i] != null && StringUtils.notBlank(values[i].toString())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将第一行的内容解析为列名
     *
     * @param values 第一行的值
     * @return 列名集合
     */
    protected String[] getColumnName(Object[] values) throws DataGetException {
        String[] columnNames = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            String columnName = StringUtils.toString(values[i]);
            if (StringUtils.isBlank(columnName)) {    //如果列头为空，那么需要给一个合适的列名
                throw new DataGetException(String.format("第%s列的列头为空", i + 1));
            } else {
                columnName = columnName.trim();
                if (ArrayUtils.contains(columnNames, columnName)) {
                    throw new DataGetException("存在重复的列名：" + columnName);
                } else {
                    columnNames[i] = columnName;
                }
            }
        }
        return columnNames;
    }
}
