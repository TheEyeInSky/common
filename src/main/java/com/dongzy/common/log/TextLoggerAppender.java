package com.dongzy.common.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.LevelFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.spi.FilterReply;
import com.dongzy.common.common.io.PathUtils;
import com.dongzy.common.common.text.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 文件日志记录器实现类，定了了将日志写入文件的规则
 *
 * @author zouyong
 * @since JDK1.6
 */
final class TextLoggerAppender extends LoggerAppenderAbstract {

    private final TextLoggerFactory loggerFactory;

    public TextLoggerAppender(TextLoggerFactory loggerFactory) {
        super();
        this.loggerFactory = loggerFactory;
    }

    private String getFullLogPath(String logFileName) {
        try {
            PathUtils.createFileDir(new File(loggerFactory.getLogPath()));
        } catch (IOException e) {
            throw new RuntimeException("Create dir : " + loggerFactory.getLogPath() + " error!");
        }
        return PathUtils.joinPath(loggerFactory.getLogPath(), "logs", logFileName);
    }

    @Override
    protected void addPersistenceAppender(Logger log, String contextName, Level level) {
        LoggerContext context = log.getLoggerContext();

        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
        rollingFileAppender.setContext(context);

        //按时间滚动策略，定义为每天一个文件，并采用zip进行压缩
        TimeBasedRollingPolicy rollingPolicy = new TimeBasedRollingPolicy();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(rollingFileAppender);
        //设置最大保存日志的日志日期为60天
        rollingPolicy.setMaxHistory(loggerFactory.getMaxHistory());

        if (StringUtils.isBlank(contextName)) {
            if (loggerFactory.isSplitFile()) {
                rollingPolicy.setFileNamePattern(getFullLogPath(String.format("%s.%%d{yyMMdd}.log.zip", level.levelStr.toLowerCase())));
            } else {
                rollingPolicy.setFileNamePattern(getFullLogPath("log.%d{yyMMdd}.log.zip"));
            }
        } else {
            if (loggerFactory.isSplitFile()) {
                rollingPolicy.setFileNamePattern(getFullLogPath(String.format("%s.%s.%%d{yyMMdd}.log.zip", contextName, level.levelStr.toLowerCase().substring(0, 1))));
            } else {
                rollingPolicy.setFileNamePattern(getFullLogPath(String.format("%s.%%d{yyMMdd}.log.zip", contextName)));
            }
        }
        rollingPolicy.start();

        LevelFilter levelFilter = new LevelFilter();
        levelFilter.setLevel(level);
        levelFilter.setContext(context);
        levelFilter.setOnMatch(FilterReply.ACCEPT);
        levelFilter.setOnMismatch(FilterReply.DENY);
        levelFilter.start();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{MM-dd HH:mm:ss} [%thread] %-5level %message%n");
        encoder.setCharset(StandardCharsets.UTF_8);
        encoder.start();

        rollingFileAppender.setEncoder(encoder);
        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.addFilter(levelFilter);
        rollingFileAppender.start();

        log.addAppender(rollingFileAppender);
    }
}
