package com.dongzy.common.log;

import com.dongzy.common.common.io.PathUtils;
import com.dongzy.common.common.io.TextFile;
import com.dongzy.common.common.text.StringUtils;
import com.dongzy.common.config.FileConfigReader;
import com.dongzy.common.config.SystemConfig;
import org.slf4j.event.Level;

import java.io.FileNotFoundException;

/**
 * 文件日志记录的实现类，实现了将日志记录文件的功能
 * 本类采用的框架为logback，是对logback进行二次封装后发布的组件
 * 本类支持的主要功能点如下：
 * 1、支持定义自定义的日记记录级别，默认为info级别以上的日志都将写入到文件中
 * 2、不同级别的日志会写入不同文件中。
 * 3、用户可以获取自定义名称的日志类，从而将特定的日志写入到特定的文件中
 * <p>
 * 如果您想配置一个只输出指定级别的日志工厂类，那么代码类似如下：
 *
 * @author zouyong
 * @since JDK1.6
 */
public final class TextLoggerFactory extends LoggerFactoryAbstract {

    private final static TextLoggerFactory TEXT_LOGGER_FACTORY = new TextLoggerFactory(StringUtils.EMPTY);
    private String logPath;          //日志记录文件的路径

    /**
     * 默认构造函数
     */
    public TextLoggerFactory(String contextName) {
        super(contextName);
        logPath = null;
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param contextName 上下文名称
     * @param level       日记记录级别
     */
    public TextLoggerFactory(String contextName, Level level) {
        super(contextName, level);
        logPath = null;
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param contextName 上下文名称
     * @param level       日记记录级别
     * @param logPath     日志的保存路径
     */
    public TextLoggerFactory(String contextName, Level level, String logPath) {
        super(contextName, level);
        this.logPath = logPath;
    }

    /**
     * 获取默认的文件日志记录工厂
     *
     * @return 文件日志记录工厂
     */
    public static TextLoggerFactory getInstance() {
        return TEXT_LOGGER_FACTORY;
    }

    /**
     * 获取日志记录的路径
     *
     * @return 日志记录的路径
     */
    synchronized public String getLogPath() {
        if (StringUtils.isBlank(logPath)) {
            try {
                logPath = SystemConfig.getConfig().getValue(DEFAULT_LOG_DIR, PathUtils.getAppPath());
                //尝试记录下文本日志的存储目录
                try {
                    try (TextFile textFile = new TextFile(PathUtils.joinPath(FileConfigReader.getConfigFileBasePath(), "logpath.txt"))) {
                        textFile.write("log path : " + logPath);
                    }
                } catch (FileNotFoundException e) {
                    //TODO
                }
            } catch (Exception ex) {
                logPath = PathUtils.getAppPath();
                System.out.println("读取日志记录路径时发生异常！");
            }
        }
        return logPath;
    }


    @Override
    protected PersistenceLogger getPersistenceLogger() {
        return new TextLogger(this);
    }

}
