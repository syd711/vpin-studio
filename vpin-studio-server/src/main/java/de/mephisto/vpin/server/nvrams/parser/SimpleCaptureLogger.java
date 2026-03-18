package de.mephisto.vpin.server.nvrams.parser;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class SimpleCaptureLogger extends SimpleLogger {

  private StringBuilder writer = new StringBuilder();

  public SimpleCaptureLogger(Class<?> clazz, LEVEL minimumLevel) {
    super(clazz, minimumLevel);
  }

  @Override
  public void log(LEVEL level, String msg, Object[] args, Throwable t) {
    if (writer != null) {
      String txt = msg.replace("{}", "%s");
      txt = String.format(txt, args);
      writer.append(txt);
      writer.append("\n");
      if (t != null) {
        writer.append(ExceptionUtils.getStackTrace(t));
        writer.append("\n");
      }
    }
  }

  public void reset() {
    writer.setLength(0);
  }

  public String getText() {
    return writer.toString();
  }
}
