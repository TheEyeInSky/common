package com.dongzy.common.common.io;

import com.gee4j.common.text.StringUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * 文本文件操作辅助类，主要包括以下功能：
 * 1、读取文本文件
 * 2、保存文本文件，如果保存文件的目录不存在，那么将会创建相关的目录
 */
public final class TextFile extends WriteFileAbstract<String> {

    private Charset charset = Charset.defaultCharset();        //当前文件编码
    private Writer writer;

    public TextFile(final OutputStream outputStream) throws IOException {
        super(outputStream);
    }

    public TextFile(String filePath) throws IOException {
        super(filePath);
    }

    public TextFile(URL fileURL) throws IOException {
        super(fileURL);
    }

    public TextFile(File file) throws IOException {
        super(file);
    }

    /**
     * 设置文件的编码
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * 读取文本文件
     *
     * @return 文件的文本内容
     * @throws IOException IO异常
     */
    synchronized public String readFile() throws IOException {

        StringBuilder sbBuilder = new StringBuilder();
        InputStreamReader streamReader = null;
        try (InputStream inputStream = getInputStream()) {
            if (charset != null) {
                streamReader = new InputStreamReader(inputStream, charset);// 考虑到编码格式
            } else {
                streamReader = new InputStreamReader(inputStream);// 考虑到编码格式
            }
            try (BufferedReader bufferedReader = new BufferedReader(streamReader)) {
                String lineTxt;
                int index = 0;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    if (index > 0) {        //如果不是第一行,那么在当前行前面添加一个换行符号
                        sbBuilder.append(StringUtils.LINE_SPEARATOR);
                    }
                    index++;
                    sbBuilder.append(lineTxt);
                }
            }
        } finally {
            if (streamReader != null)
                streamReader.close();
        }

        return sbBuilder.toString();
    }

    @Override
    synchronized public void write(String content) throws IOException {
        if (StringUtils.notEmpty(content)) {
            try {
                tryRenameOldFile();
                getWriter().write(content);
                getWriter().flush();
            } finally {
                close();
            }
        }
    }

    //获取文本读写对象
    private Writer getWriter() throws IOException {
        if (writer == null) {
            writer = new OutputStreamWriter(getOutputStream(), charset);
        }
        return writer;
    }

    @Override
    public void close() throws IOException {
        if (writer != null) {
            try {
                writer.close();
            } finally {
                writer = null;
            }
        }
        super.close();
    }

}
