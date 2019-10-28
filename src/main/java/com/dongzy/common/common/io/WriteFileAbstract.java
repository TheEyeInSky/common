package com.dongzy.common.common.io;

import java.io.*;
import java.net.URL;

/**
 * 文件写入的抽象类,提供写入文件的基本封装
 *
 * @author zouyong
 */
public abstract class WriteFileAbstract<T extends Serializable> implements Closeable {

    private File file;                          //传入文件
    private OutputStream outputStream;       //传入的流
    private OutputStream fileOutputStream;      //文件输出流
    private InputStream fileInputStream;        //文件输入流
    private boolean backupFile = false;
    private boolean append = false;             //是否为内容追加模式，默认为否

    /**
     * 根据传入的参数构造函数
     *
     * @param outputStream 输出流
     */
    public WriteFileAbstract(final OutputStream outputStream) throws IOException {
        this.outputStream = outputStream;
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param filePath 文件的完整路径
     */
    public WriteFileAbstract(String filePath) throws IOException {
        this(new File(filePath));
    }

    /**
     * @param fileURL 文件URL
     * @throws IOException
     */
    public WriteFileAbstract(URL fileURL) throws IOException {
        String path = PathUtils.fromUrl(fileURL).toString();
        initFile(new File(path));
    }

    /**
     * @param file file对象
     */
    public WriteFileAbstract(File file) throws IOException {
        initFile(file);
    }

    /**
     * 设置是否保存前备份旧的文件
     *
     * @param backupFile 是否保存前备份旧的文件
     */
    public void setBackupFile(boolean backupFile) {
        this.backupFile = backupFile;
    }

    /**
     * 设置是否内容为追加模式。默认为覆盖模式，写入的内容会完全覆盖之前文件的内容
     * 如果希望内容为追加模式，那么可以设置为true，那么新增内容会添加在文件的末尾。
     *
     * @param append 是否为追加模式
     */
    public void setAppend(boolean append) {
        this.append = append;
    }

    /**
     * 获取输出流
     *
     * @return 输出流
     * @throws FileNotFoundException 没有找到写入的文件
     */
    protected OutputStream getOutputStream() throws FileNotFoundException {
        if (fileOutputStream != null) {
            return fileOutputStream;
        } else if (file != null) {
            fileOutputStream = new FileOutputStream(file, append);
            return fileOutputStream;
        } else {
            return outputStream;
        }
    }

    /**
     * 获取输入流
     *
     * @return 输入流
     * @throws FileNotFoundException 没有找到写入的文件
     */
    protected InputStream getInputStream() throws FileNotFoundException {
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        fileInputStream = new FileInputStream(file);
        return fileInputStream;
    }

    /**
     * 获取构造其中传入的文件对象
     *
     * @return 文件对象
     */
    protected File getFile() {
        return file;
    }

    /**
     * 写入当个对象
     *
     * @param object 需要写入的对象
     * @throws IOException 文件写入异常
     */
    public abstract void write(T object) throws IOException;

    //初始化文件路径
    private void initFile(File file) throws IOException {
        this.file = file;
        //创建目录
        PathUtils.createFileDir(file);
    }

    /**
     * 对已经存在的文件进行重命名
     */
    protected void tryRenameOldFile() {

        //备份的条件为：启用了备份和文件存在两个条件满足时才备份
        if (backupFile && file != null && file.exists()) {
            File backupFile;
            do {
                String name = file.getAbsolutePath();
                int place = name.lastIndexOf('.');
                backupFile = new File(String.format("%s.%s.%s", name.substring(0, place),
                        System.currentTimeMillis(), name.substring(place + 1)));
            } while (backupFile.exists());

            file.renameTo(backupFile);
        }
    }

    /**
     * 如果采用文件参数作为输出，那么关闭关闭输出流
     * 如果采用输出流作为输出，那么不能关闭输出流
     */
    @Override
    public void close() throws IOException {
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close();
            } finally {
                fileOutputStream = null;
            }
        }
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } finally {
                fileInputStream = null;
            }
        }
    }

    @Override
    protected void finalize() throws IOException {
        close();
    }
}
