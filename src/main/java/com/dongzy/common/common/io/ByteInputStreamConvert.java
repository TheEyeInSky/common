package com.dongzy.common.common.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 二进制数组与Stream对象的互转
 *
 * @author zouyong
 * @since JDK1.5
 */
public final class ByteInputStreamConvert {

    /**
     * 二进制数组转换成Stream对象
     *
     * @param bytes 二进制数组
     * @return 流对象
     */
    public static InputStream bytes2InputStream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Stream对象转换成二进制数组
     *
     * @param inStream 输入流
     * @return 二进制数组
     * @throws IOException 读取流异常
     */
    public static byte[] inputStream2Bytes(InputStream inStream) throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[2048];
        int len;
        while ((len = inStream.read(buff)) != -1) {
            swapStream.write(buff, 0, len);
        }
        return swapStream.toByteArray();
    }
}
