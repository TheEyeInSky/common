package com.dongzy.common.common.text;

import com.dongzy.common.common.io.ClassLoaderWrapper;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * XML文件读取辅助工具类，主要包括以下功能：
 * 1、读取xml文件，可以设置是否检查下面了文件的格式是否符合模式，默认不进行检查
 * 2、保存XML内容到磁盘
 *
 * @author zouyong
 * @since JDK1.5
 */
public final class XmlDocumentUtils {

    //匹配带盘符的绝对路径
    private final static Pattern PATTERN = Pattern.compile("^[a-zA-Z]:[\\\\/].*?$");

    /**
     * 获取指定的xml文档
     *
     * @param resourceName 读取的文件名（不包含路径信息）
     */
    public static Document readFile(String resourceName) throws IOException {
        return readFile(resourceName, false);
    }

    /**
     * 获取指定的xml文档
     *
     * @param resourceName   读取的文件名（不包含路径信息）
     * @param isVerifyStruct 是否校验XML文件的结构
     */
    public static Document readFile(String resourceName, boolean isVerifyStruct) throws IOException {

        if (PATTERN.matcher(resourceName).matches()) { //如果路径是绝对路径，那么
            try (InputStream inputStream = new FileInputStream(resourceName)) {
                return read(inputStream, isVerifyStruct);
            }
        } else {        //如果提供的是资源文件名，那么
            try (InputStream inputStream = new ClassLoaderWrapper().getResourceStream(resourceName)) {
                return read(inputStream, isVerifyStruct);
            }
        }
    }

    /**
     * 获取指定的xml文档
     *
     * @param file 读取文件类
     */
    public static Document readFile(File file) throws IOException {
        return readFile(file, false);
    }

    /**
     * 获取指定的xml文档
     *
     * @param file           读取文件类
     * @param isVerifyStruct 是否校验XML文件的结构
     */
    public static Document readFile(File file, boolean isVerifyStruct) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            return read(inputStream, isVerifyStruct);
        }
    }

    /**
     * 从字符串中读取XML文件的内容
     *
     * @param xmlString xml文档的内容
     * @return Document对象
     * @throws IOException 读取文件时发生异常
     */
    public static Document read(String xmlString) throws IOException {
        return read(xmlString, null, false);
    }

    /**
     * 从字符串中读取XML文件的内容
     *
     * @param xmlString xml文档的内容
     * @param encoding  文件编码
     * @return Document对象
     * @throws IOException 读取文件时发生异常
     */
    public static Document read(String xmlString, String encoding, boolean isVerifyStruct) throws IOException {
        if (StringUtils.isEmpty(encoding)) {
            encoding = "UTF-8";
        }
        InputStream stream = new ByteArrayInputStream(xmlString.getBytes(encoding));
        return read(stream, isVerifyStruct);
    }

    /**
     * 从流中读取XML文件的内容
     *
     * @param inputStream    输入流
     * @param isVerifyStruct 是否校验XML文件的格式
     * @return Document对象
     * @throws IOException 读取文件时发生异常
     */
    public static Document read(InputStream inputStream, boolean isVerifyStruct) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            if (!isVerifyStruct) {
                builder.setEntityResolver((arg0, arg1) -> new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes())));
            }
            Document document = builder.parse(inputStream);
            document.setXmlVersion("1.0");
            return document;
        } catch (Exception ex) {
            throw new IOException("无法正确解析XML文件的内容！");
        }
    }

    /**
     * 保存XML文件，采用UTF-8编码
     *
     * @param document 需要保存的文档
     * @param file     文明路径
     */
    public static void save(Document document, File file) throws IOException, TransformerException {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transFormer = transFactory.newTransformer();
            transFormer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            Source source = new DOMSource(document);
            Result result = new StreamResult(outputStream);
            transFormer.transform(source, result);
            outputStream.flush();
        }
    }

    /**
     * 对普通文本进行xml编码，编码成CDATA格式
     *
     * @param string 需要编码的字符串
     */
    public static String encode2CDATA(String string) {
        if (string == null) {
            return StringUtils.EMPTY;
        }

        if (string.contains(">") || string.contains("<") || string.contains("&") ||
                string.contains("\"") || string.contains("'")) {
            string = String.format("<![CDATA[ %s ]]>", string);
        }
        return string;
    }

    /**
     * 对普通文本进行xml编码，编码生可以在属性里面的格式
     *
     * @param string 需要编码的字符串
     */
    public static String encode2Property(String string) {
        if (string == null) {
            return StringUtils.EMPTY;
        }

        //&必须放在第一个，因为其他替换会产生&符号
        string = string.replaceAll("&", "&amp;");

        string = string.replaceAll(">", "&gt;");
        string = string.replaceAll("<", "&lt;");
        string = string.replaceAll("\"", "&quot;");
        return string;
    }
}
