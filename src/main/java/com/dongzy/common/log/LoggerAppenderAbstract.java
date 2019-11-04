package com.dongzy.common.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.LevelFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.spi.FilterReply;

import java.nio.charset.StandardCharsets;

/**
 * 文件日志记录器实现类，定了了将日志写入文件的规则
 *
 * @author zouyong
 * @since JDK1.6
 */
public abstract class LoggerAppenderAbstract {

    /**
     * 默认构造函数
     */
    public LoggerAppenderAbstract() {
        super();
    }

    /**
     * 添加日志记录器
     *
     * @param log         日志记录的类
     * @param contextName 上下文名称
     * @param level       日志记录的级别
     */
    public void addRecordAppender(Logger log, String contextName, Level level) {

        //输出到控制台输出
        addConsoleAppender(log, level);

        switch (level.levelInt) {
            case Level.ERROR_INT:
            case Level.WARN_INT:
            case Level.INFO_INT:
            case Level.DEBUG_INT:
                //以上四类需要持久化
                addPersistenceAppender(log, contextName, level);
                break;
            default:
                break;
        }
    }

    /**
     * 定义持久化日志的添加器
     *
     * @param log         日志记录类
     * @param contextName 上下文名称
     * @param level       日志的级别
     */
    protected abstract void addPersistenceAppender(Logger log, String contextName, Level level);

    /**
     * 配置控制台输出样式
     *
     * @param log   日志记录类
     * @param level 日志的级别
     */
    protected void addConsoleAppender(Logger log, Level level) {
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(log.getLoggerContext());
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(log.getLoggerContext());
        switch (level.levelInt) {
            case Level.TRACE_INT:
            case Level.DEBUG_INT:
            case Level.INFO_INT:
                encoder.setPattern("%d{HH:mm:ss.SSS} %message %n");
                break;
            case Level.WARN_INT:
                encoder.setPattern("%magenta(%d{HH:mm:ss.SSS} %message) %n");
                break;
            case Level.ERROR_INT:
                encoder.setPattern("%red(%d{HH:mm:ss.SSS} %message) %n");
                break;
        }
        encoder.setCharset(StandardCharsets.UTF_8);

        encoder.start();
        consoleAppender.setEncoder(encoder);

        LevelFilter levelFilter = new LevelFilter();
        levelFilter.setLevel(level);
        levelFilter.setContext(log.getLoggerContext());
        levelFilter.setOnMatch(FilterReply.ACCEPT);
        levelFilter.setOnMismatch(FilterReply.DENY);
        levelFilter.start();

        consoleAppender.addFilter(levelFilter);
        consoleAppender.start();
        log.addAppender(consoleAppender);
    }
}
