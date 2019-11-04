package com.dongzy.common.common.io;

import org.slf4j.Logger;
import com.dongzy.common.common.Validate;
import com.dongzy.common.log.TextLoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * 用于采用文件作为锁的实现类，用于确保系统进程唯一的类。
 * 因为同一时间，文件只会被一个进程锁定。
 */
public class FileLockHelper {

    private static final Logger LOGGER = TextLoggerFactory.getInstance().getLogger(FileLockHelper.class);
    private static RandomAccessFile raf;
    private static FileChannel channel;
    private static FileLock lock;

    /**
     * 根据传入的参数构造函数
     *
     * @param file 文件对象
     */
    public FileLockHelper(File file) throws FileNotFoundException {
        init(file, false);
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param file       文件对象
     * @param createFile 如果该文件不存在，是否创建该文件
     */
    public FileLockHelper(File file, boolean createFile) throws FileNotFoundException {
        init(file, createFile);
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param resrouceName 资源文件名称
     */
    public FileLockHelper(String resrouceName) throws FileNotFoundException {
        File file = new ClassLoaderWrapper().getResourceFile(resrouceName);
        init(file, false);
    }

    private void init(File file, boolean createFile) throws FileNotFoundException {
        Validate.notNull(file);

        if (createFile && !file.exists()) {         //如果没有该文件，那么创建改文件
            try (TextFile textFile = new TextFile(file)) {
                textFile.write("lockfile.");
            } catch (IOException e) {
                LOGGER.error("在创建锁定文件" + file + "时发生异常！", e);
            }
        }

        raf = new RandomAccessFile(file, "rw");
        channel = raf.getChannel();
    }

    /**
     * 尝试锁定指定的文件
     *
     * @return 是否成功锁定文件
     */
    public boolean tryLock() {
        try {
            lock = channel.tryLock();
        } catch (IOException e1) {
            LOGGER.error("锁定文件时发生未知错误！", e1);
        }
        return lock != null;        //如果lock不为空，表示锁定文件成功。
    }

    /**
     * 解除文件的锁定状态
     */
    public void unlock() {
        //释放文件锁
        try {
            lock.release();
        } catch (Exception ex) {
            LOGGER.error("释放FileLock时发生异常！", ex);
        }
        //释放文件锁
        try {
            channel.close();
        } catch (Exception ex) {
            LOGGER.error("释放FileChannel时发生异常！", ex);
        }
        //释放文件锁
        try {
            raf.close();
        } catch (Exception ex) {
            LOGGER.error("释放RandomAccessFile时发生异常！", ex);
        }
    }
}
