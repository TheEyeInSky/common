package com.dongzy.common.common.io;

import java.io.*;
import java.net.URL;

/**
 * 二进制文件操作辅助类，主要包括以下功能：
 * 1、读取二进制文件
 * 2、保存二进制文件，如果保存文件的目录不存在，那么将会创建相关的目录
 */
public final class BinaryFile<T extends Serializable> extends WriteFileAbstract<T> {

    public BinaryFile(final OutputStream outputStream) throws IOException {
        super(outputStream);
    }

    public BinaryFile(String filePath) throws IOException {
        super(filePath);
    }

    public BinaryFile(URL fileURL) throws IOException {
        super(fileURL);
    }

    public BinaryFile(File file) throws IOException {
        super(file);
    }

    /**
     * 读取二进制文件
     *
     * @return 二进制文件对应的对象
     * @throws IOException IO异常
     */
    public T readFile() throws IOException {
        try (InputStream inputStream = getInputStream()) {
            try (ObjectInputStream objInput = new ObjectInputStream(inputStream)) {
                Object object = objInput.readObject();
                return (T) object;
            } catch (ClassNotFoundException e) {
                throw new IOException("无法将文件的二进制内容转换为对象！");
            }
        }
    }

    /**
     * 保存对象到二进制文件
     *
     * @param object 需要写入的对象
     * @throws IOException IO异常
     */
    @Override
    synchronized public void write(T object) throws IOException {
        if (object != null) {
            try {
                tryRenameOldFile();
                getOutputStream().write(SerializationUtils.serialize(object));
            } finally {
                close();
            }
        }
    }
}
