package de.mephisto.vpin.server.nvrams.decoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MarkerIgnoringBase;

public abstract class SimpleLogger extends MarkerIgnoringBase {

  private Logger delegate;

  public enum LEVEL { TRACE, DEBUG, INFO, WARN, ERROR };

  public LEVEL minimumLevel;

  public SimpleLogger(Class<?> clazz, LEVEL minimumLevel) {
    this.delegate = LoggerFactory.getLogger(clazz);
    this.minimumLevel = minimumLevel;
  }

  public abstract void log(LEVEL trace, String msg, Object[] args, Throwable t);

  //-------------------------------------------

  @Override
  public boolean isTraceEnabled() {
    return LEVEL.TRACE.compareTo(minimumLevel) >= 0;
  }

  @Override
  public void trace(String msg) {
    delegate.trace(msg);
    if (isTraceEnabled()) {
      log(LEVEL.TRACE, msg, null, null);
    }
  }

  @Override
  public void trace(String format, Object arg) {
    delegate.trace(format, arg);
    if (isTraceEnabled()) {
      log(LEVEL.TRACE, format, new Object[] { arg }, null);
    }
  }

  @Override
  public void trace(String format, Object arg1, Object arg2) {
    delegate.trace(format, arg1, arg2);
    if (isTraceEnabled()) {
      log(LEVEL.TRACE, format, new Object[] { arg1, arg2 }, null);
    }
  }

  @Override
  public void trace(String format, Object... arguments) {
    delegate.trace(format, arguments);
    if (isTraceEnabled()) {
      log(LEVEL.TRACE, format, arguments, null);
    }
  }

  @Override
  public void trace(String msg, Throwable t) {
    delegate.trace(msg, t);
    if (isTraceEnabled()) {
      log(LEVEL.TRACE, msg, null, t);
    }
  }

  @Override
  public boolean isDebugEnabled() {
    return LEVEL.DEBUG.compareTo(minimumLevel) >= 0;
  }

  @Override
  public void debug(String msg) {
    delegate.debug(msg);
    if (isDebugEnabled()) {
      log(LEVEL.DEBUG, msg, null, null);
    }
  }

  @Override
  public void debug(String format, Object arg) {
    delegate.debug(format, arg);
    if (isDebugEnabled()) {
      log(LEVEL.DEBUG, format, new Object[] { arg }, null);
    }
  }

  @Override
  public void debug(String format, Object arg1, Object arg2) {
    delegate.debug(format, arg1, arg2);
    if (isDebugEnabled()) {
      log(LEVEL.DEBUG, format, new Object[] { arg1, arg2 }, null);
    }
  }

  @Override
  public void debug(String format, Object... arguments) {
    delegate.debug(format, arguments);
    if (isDebugEnabled()) {
      log(LEVEL.DEBUG, format, arguments, null);
    }
  }

  @Override
  public void debug(String msg, Throwable t) {
     delegate.debug(msg, t);
    if (isDebugEnabled()) {
     log(LEVEL.DEBUG, msg, null, t);
    }
  }

  @Override
  public boolean isInfoEnabled() {
    return LEVEL.INFO.compareTo(minimumLevel) >= 0;
  }

  @Override
  public void info(String msg) {
    delegate.info(msg);
    if (isInfoEnabled()) {
      log(LEVEL.INFO, msg, null, null);
    }
  }

  @Override
  public void info(String format, Object arg) {
    delegate.info(format, arg);
    if (isInfoEnabled()) {
      log(LEVEL.INFO, format, new Object[] { arg }, null);
    }
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    delegate.info(format, arg1, arg2);
    if (isInfoEnabled()) {
      log(LEVEL.INFO, format, new Object[] { arg1, arg2 }, null);
    }
  }

  @Override
  public void info(String format, Object... arguments) {
    delegate.info(format, arguments);
    if (isInfoEnabled()) {
      log(LEVEL.INFO, format, arguments, null);
    }
  }

  @Override
  public void info(String msg, Throwable t) {
    delegate.info(msg, t);
    if (isInfoEnabled()) {
      log(LEVEL.INFO, msg, null, t);
    }
  }

  @Override
  public boolean isWarnEnabled() {
    return LEVEL.WARN.compareTo(minimumLevel) >= 0;
  }

  @Override
  public void warn(String msg) {
    delegate.warn(msg);
    if (isWarnEnabled()) {
      log(LEVEL.WARN, msg, null, null);
    }
  }

  @Override
  public void warn(String format, Object arg) {
    delegate.warn(format, arg);
    if (isWarnEnabled()) {
      log(LEVEL.WARN, format, new Object[] { arg }, null);
    }
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    delegate.warn(format, arg1, arg2);
    if (isWarnEnabled()) {
      log(LEVEL.WARN, format, new Object[] { arg1, arg2 }, null);
    }
  }

  @Override
  public void warn(String format, Object... arguments) {
    delegate.warn(format, arguments);
    if (isWarnEnabled()) {
      log(LEVEL.WARN, format, arguments, null);
    }
  }

  @Override
  public void warn(String msg, Throwable t) {
    delegate.warn(msg, t);
    if (isWarnEnabled()) {
      log(LEVEL.WARN, msg, null, t);
    }
  }

  @Override
  public boolean isErrorEnabled() {
    return LEVEL.ERROR.compareTo(minimumLevel) >= 0;
  }

  @Override
  public void error(String msg) {
    delegate.error(msg);
    if (isErrorEnabled()) {
      log(LEVEL.ERROR, msg, null, null);
    }
  }

  @Override
  public void error(String format, Object arg) {
    delegate.error(format, arg);
    if (isErrorEnabled()) {
      log(LEVEL.ERROR, format, new Object[] { arg }, null);
    }
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    delegate.error(format, arg1, arg2);
    if (isErrorEnabled()) {
      log(LEVEL.ERROR, format, new Object[] { arg1, arg2 }, null);
    }
  }

  @Override
  public void error(String format, Object... arguments) {
    delegate.error(format, arguments);
    if (isErrorEnabled()) {
      log(LEVEL.ERROR, format, arguments, null);
    }
  }

  @Override
  public void error(String msg, Throwable t) {
    delegate.error(msg, t);
    if (isErrorEnabled()) {
      log(LEVEL.ERROR, msg, null, t);
    }
  }
}
