package com.dongzy.common.log;

/**
 * 采用文本文件的形式记录日志
 */
final class TextLogger extends PersistenceLogger {

    public TextLogger(TextLoggerFactory loggerFactory) {
        super(loggerFactory.getContextName());
        setAppender(new TextLoggerAppender(loggerFactory));
    }

}
