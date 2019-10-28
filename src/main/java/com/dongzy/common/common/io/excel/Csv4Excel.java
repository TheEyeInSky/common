package com.dongzy.common.common.io.excel;

import org.slf4j.Logger;
import com.gee4j.common.collection.CollectionUtils;
import com.gee4j.common.io.WriteFileAbstract;
import com.gee4j.common.io.csv.CSVFormat;
import com.gee4j.common.io.csv.CSVParser;
import com.gee4j.common.io.csv.CSVPrinter;
import com.gee4j.common.io.csv.CSVRecord;
import com.gee4j.data.table.DataRow;
import com.gee4j.data.table.DataTable;
import com.gee4j.log.TextLoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * CSV文件读写支持类
 */
public final class Csv4Excel extends WriteFileAbstract<DataTable> {

    private final static Logger LOGGER = TextLoggerFactory.getInstance().getLogger(Csv4Excel.class);
    private final Charset charset = Charset.forName("gb18030");        //当前文件编码
    private final CSVFormat format = CSVFormat.EXCEL;
    private Writer writer;
    private CSVPrinter csvPrinter;

    public Csv4Excel(final OutputStream outputStream) throws IOException {
        super(outputStream);
    }

    public Csv4Excel(String filePath) throws IOException {
        super(filePath);
    }

    public Csv4Excel(URL fileURL) throws IOException {
        super(fileURL);
    }

    public Csv4Excel(File file) throws IOException {
        super(file);
    }

    /**
     * 读取文本文件到一个表格数据中
     *
     * @return 文件的文本内容
     * @throws IOException IO异常
     */
    synchronized public DataTable readFile() throws IOException {

        Reader reader = null;
        CSVParser parser = null;
        try (InputStream inputStream = getInputStream()) {
            reader = new InputStreamReader(inputStream, charset);// 考虑到编码格式
            parser = new CSVParser(reader, format.withHeader());
            DataTable dataTable = new DataTable(CollectionUtils.toArray(parser.getHeaderMap().keySet()));
            for (CSVRecord record : parser) {
                dataTable.append(dataTable.newRow(record.values()));
            }
            return dataTable;
        } catch (IOException e) {
            LOGGER.error("读取CSV文件时发生出错!", e);
            throw new IOException("读取文件内容时发生出错，详细内容见日志");
        } finally {
            if (parser != null)
                parser.close();
            if (reader != null)
                reader.close();
        }
    }

    /**
     * 写入文本的内容
     *
     * @param dataTable 需要写入的内容
     * @throws IOException
     */
    @Override
    synchronized public void write(DataTable dataTable) throws IOException {
        if (dataTable != null) {

            try {
                tryRenameOldFile();

                final CSVPrinter printer = getWriter();

                for (String value : dataTable.columnNames()) {
                    printer.print(value);
                }
                printer.println();

                for (DataRow dataRow : dataTable.dataRows()) {
                    for (Object value : dataRow.getValues()) {
                        printer.print(value);
                    }
                    printer.println();
                }
                printer.flush();
            } finally {
                close();
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (csvPrinter != null) {
            try {
                csvPrinter.close();
            } finally {
                csvPrinter = null;
            }
        }
        if (writer != null) {
            try {
                writer.close();
            } finally {
                writer.close();
            }
        }
        super.close();
    }

    //获取数据写入的类
    private CSVPrinter getWriter() throws IOException {
        if (csvPrinter == null) {
            writer = new OutputStreamWriter(getOutputStream(), charset);
            csvPrinter = new CSVPrinter(writer, format);
        }
        return csvPrinter;
    }
}
