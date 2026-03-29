package de.mephisto.vpin.server.nvrams.parser;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class SimpleFileLogger extends SimpleLogger implements Closeable {

  private PrintWriter writer;

  public SimpleFileLogger(Class<?> clazz, LEVEL minimumLevel, File file) {
    super(clazz, minimumLevel);
    try {
      this.writer = new PrintWriter(new FileWriter(file));
    }
    catch (IOException ioe) {}
  }

  @Override
  public void log(LEVEL level, String msg, Object[] args, Throwable t) {
    if (writer != null) {
      String txt = msg.replace("{}", "%s");
      txt = String.format(txt, args) + "\n";
      writer.write(txt);
      if (t != null) {
        t.printStackTrace(writer);
      }
    }
  }

  @Override
  public void close() throws IOException {
    if (writer != null) {
      writer.close();
    }
  }
}
