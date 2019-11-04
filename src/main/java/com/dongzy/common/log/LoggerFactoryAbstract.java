package com.dongzy.common.log;

import ch.qos.logback.classic.LoggerContext;
import com.dongzy.common.common.Validate;
import com.dongzy.common.common.text.StringUtils;
import com.dongzy.common.config.CustomConfigManager;
import com.dongzy.common.config.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
public abstract class LoggerFactoryAbstract implements ILoggerFactory {

    /**
     * 记录日志的默认目录
     */
    static final String DEFAULT_LOG_DIR = "log.record.dir";
    /**
     * 默认日志记录器的默认日志记录级别
     */
    static final String DEFAULT_LOG_LEVEL = "log.record.level";
    /**
     * 是否按照不同的日志级别拆分到不同的文件中
     */
    static final String LOG_SPLITE_FILE = "log.record.splitfile";
    /**
     * 配置jpa日志的日志记录级别，默认为OFF，不记录日志
     */
    static final String RECORD_JPA_LOG = "log.jpa.level";

    private final static Map<String, LoggerContext> CONTEXT_MAP = new ConcurrentHashMap<>();
    private final ch.qos.logback.classic.Level level;
    private final String contextName;

    //是否将不同的日志级别的内容是否存储到不同文件中
    private boolean splitFile = CustomConfigManager.getCustomConfig().getBooleanValue(LOG_SPLITE_FILE, false);
    private int maxHistory;           //保留日志的最大天数

    /**
     * 默认构造函数
     */
    public LoggerFactoryAbstract(String contextName) {
        Validate.notNull(contextName, "log上下文名称不能为null");
        this.contextName = contextName;

        String propertyName = StringUtils.isBlank(contextName) ? DEFAULT_LOG_LEVEL : DEFAULT_LOG_LEVEL + "." + contextName;

        //初始化日志记录的级别
        String levelString = SystemConfig.getConfig().getValue(propertyName, "info");
        switch (levelString) {
            case "trace":
                level = ch.qos.logback.classic.Level.TRACE;
                break;
            case "debug":
                level = ch.qos.logback.classic.Level.DEBUG;
                break;
            case "warn":
                level = ch.qos.logback.classic.Level.WARN;
                break;
            case "error":
                level = ch.qos.logback.classic.Level.ERROR;
                break;
            default:
                level = ch.qos.logback.classic.Level.INFO;
                break;
        }
    }

    /**
     * 默认构造函数
     */
    public LoggerFactoryAbstract(String contextName, Level level) {
        this.contextName = contextName;
        this.level = ch.qos.logback.classic.Level.valueOf(level.name());
    }

    @Override
    public String getContextName() {
        return contextName;
    }

    @Override
    public boolean isSplitFile() {
        return splitFile;
    }

    @Override
    public void setSplitFile(boolean splitFile) {
        this.splitFile = splitFile;
    }

    @Override
    public void setMaxHistory(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    @Override
    public int getMaxHistory() {
        return maxHistory;
    }

    @Override
    public Logger getLogger(final Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    @Override
    @Deprecated
    public Logger getLogger(String contextName, final Class<?> clazz) {
        throw new IllegalStateException("本方法已经不再被支持，请该调用getLogger(final Class<?> clazz)方法");
    }

    @Override
    public Logger getLogger(String loggerName) {
        LoggerContext loggerContext = CONTEXT_MAP.get(contextName);
        if (loggerContext == null) {
            loggerContext = new LoggerContext();
            loggerContext.setName(contextName);
            CONTEXT_MAP.put(contextName, loggerContext);
        }

        ch.qos.logback.classic.Logger log = loggerContext.getLogger(loggerName);
        log.setLevel(level);
        PersistenceLogger fileLogger = getPersistenceLogger();
        fileLogger.setLogger(log);
        return fileLogger;
    }

    /**
     * 获取持久化的日志记录类
     *
     * @return 持久化日志记录类
     */
    protected abstract PersistenceLogger getPersistenceLogger();
}
