package com.dongzy.common.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.Marker;

/**
 * Logger的代理类
 */
public abstract class PersistenceLogger implements org.slf4j.Logger {

    private Logger logger;
    private final String contextName;
    private volatile boolean isInitTrace;
    private volatile boolean isInitDebug;
    private volatile boolean isInitInfo;
    private volatile boolean isInitWarn;
    private volatile boolean isInitError;
    private LoggerAppenderAbstract appender;

    public PersistenceLogger(String contextName) {
        this.contextName = contextName;
    }

    public void setAppender(LoggerAppenderAbstract appender) {
        this.appender = appender;
    }

    private synchronized void initTraceAppender() {
        if (!isInitTrace) {
            if (logger.getLevel().levelInt <= Level.TRACE_INT) {
                appender.addRecordAppender(logger, contextName, Level.TRACE);
            }
            isInitTrace = true;
        }
    }

    private synchronized void initDebugAppender() {
        if (!isInitDebug) {
            if (logger.getLevel().levelInt <= Level.DEBUG_INT) {
                appender.addRecordAppender(logger, contextName, Level.DEBUG);
            }
            isInitDebug = true;
        }
    }

    private synchronized void initInfoAppender() {
        if (!isInitInfo) {
            if (logger.getLevel().levelInt <= Level.INFO_INT) {
                appender.addRecordAppender(logger, contextName, Level.INFO);
            }
            isInitInfo = true;
        }
    }

    private synchronized void initWarnAppender() {
        if (!isInitWarn) {
            if (logger.getLevel().levelInt <= Level.WARN_INT) {
                appender.addRecordAppender(logger, contextName, Level.WARN);
            }
            isInitWarn = true;
        }
    }

    private synchronized void initErrorAppender() {
        if (!isInitError) {
            if (logger.getLevel().levelInt <= Level.ERROR_INT) {
                appender.addRecordAppender(logger, contextName, Level.ERROR);
            }
            isInitError = true;
        }
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        initTraceAppender();
        logger.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        initTraceAppender();
        logger.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        initTraceAppender();
        logger.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        initTraceAppender();
        logger.trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        initTraceAppender();
        logger.trace(msg, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return logger.isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String msg) {
        initTraceAppender();
        logger.trace(marker, msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        initTraceAppender();
        logger.trace(marker, format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        initTraceAppender();
        logger.trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        initTraceAppender();
        logger.trace(marker, format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        initTraceAppender();
        logger.trace(marker, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        initTraceAppender();
        initDebugAppender();
        logger.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        initTraceAppender();
        initDebugAppender();
        logger.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        initTraceAppender();
        initDebugAppender();
        logger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        initTraceAppender();
        initDebugAppender();
        logger.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        initTraceAppender();
        initDebugAppender();
        logger.debug(msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return logger.isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String msg) {
        initTraceAppender();
        initDebugAppender();
        logger.debug(marker, msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        initTraceAppender();
        initDebugAppender();
        logger.debug(marker, format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        initTraceAppender();
        initDebugAppender();
        logger.debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... argArray) {
        initTraceAppender();
        initDebugAppender();
        logger.debug(marker, format, argArray);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        initTraceAppender();
        initDebugAppender();
        logger.debug(marker, msg, t);
    }


    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        initTraceAppender();
        initInfoAppender();
        logger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        initTraceAppender();
        initInfoAppender();
        logger.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        initTraceAppender();
        initInfoAppender();
        logger.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        initTraceAppender();
        initInfoAppender();
        logger.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        initTraceAppender();
        initInfoAppender();
        logger.info(msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return logger.isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String msg) {
        initTraceAppender();
        initInfoAppender();
        logger.info(marker, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        initTraceAppender();
        initInfoAppender();
        logger.info(marker, format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        initTraceAppender();
        initInfoAppender();
        logger.info(marker, format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... argArray) {
        initTraceAppender();
        initInfoAppender();
        logger.info(marker, format, argArray);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        initTraceAppender();
        initInfoAppender();
        logger.info(marker, msg, t);
    }


    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        initTraceAppender();
        initWarnAppender();
        logger.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        initTraceAppender();
        initWarnAppender();
        logger.warn(format, arg);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        initTraceAppender();
        initWarnAppender();
        logger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String format, Object... arguments) {
        initTraceAppender();
        initWarnAppender();
        logger.warn(format, arguments);
    }

    @Override
    public void warn(String msg, Throwable t) {
        initTraceAppender();
        initWarnAppender();
        logger.warn(msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return logger.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String msg) {
        initTraceAppender();
        initWarnAppender();
        logger.warn(marker, msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        initTraceAppender();
        initWarnAppender();
        logger.warn(marker, format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        initTraceAppender();
        initWarnAppender();
        logger.warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... argArray) {
        initTraceAppender();
        initWarnAppender();
        logger.warn(marker, format, argArray);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        initTraceAppender();
        initWarnAppender();
        logger.warn(marker, msg, t);
    }


    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        initTraceAppender();
        initErrorAppender();
        logger.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        initTraceAppender();
        initErrorAppender();
        logger.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        initTraceAppender();
        initErrorAppender();
        logger.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        initTraceAppender();
        initErrorAppender();
        logger.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        initTraceAppender();
        initErrorAppender();
        logger.error(msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return logger.isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String msg) {
        initTraceAppender();
        initErrorAppender();
        logger.error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        initTraceAppender();
        initErrorAppender();
        logger.error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        initTraceAppender();
        initErrorAppender();
        logger.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... argArray) {
        initTraceAppender();
        initErrorAppender();
        logger.error(marker, format, argArray);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        initTraceAppender();
        initErrorAppender();
        logger.error(marker, msg, t);
    }
}
