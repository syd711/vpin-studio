package de.mephisto.vpin.restclient.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;

/**
 * This class is intended to be used with the SystemCommandExecutor
 * class to let users execute system commands from Java applications.
 * <p/>
 * This class is based on work that was shared in a JavaWorld article
 * named "When System.exec() won't". That article is available at this
 * url:
 * <p/>
 * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
 * <p/>
 * Documentation for this class is available at this URL:
 * <p/>
 * http://devdaily.com/java/java-processbuilder-process-system-exec
 * <p/>
 * <p/>
 * Copyright 2010 alvin j. alexander, devdaily.com.
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Please ee the following page for the LGPL license:
 * http://www.gnu.org/licenses/lgpl.txt
 */
class ThreadedStreamHandler extends Thread {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  InputStream inputStream;
  String adminPassword;
  OutputStream outputStream;
  PrintWriter printWriter;
  StringBuilder outputBuffer = new StringBuilder();
  private boolean sudoIsRequested = false;
  private boolean enableLog = false;

  private boolean stopped = false;

  /**
   * A simple constructor for when the sudo command is not necessary.
   * This constructor will just run the command you provide, without
   * running sudo before the command, and without expecting a password.
   *
   * @param inputStream
   */
  ThreadedStreamHandler(String name, InputStream inputStream) {
    super(name);
    this.inputStream = inputStream;
  }

  /**
   * Use this constructor when you want to invoke the 'sudo' command.
   * The outputStream must not be null. If it is, you'll regret it. :)
   * <p/>
   *
   * @param name
   * @param inputStream
   * @param outputStream
   * @param adminPassword
   */
  ThreadedStreamHandler(String name, InputStream inputStream, OutputStream outputStream, String adminPassword) {
    super(name);
    this.inputStream = inputStream;
    this.outputStream = outputStream;
    this.printWriter = new PrintWriter(outputStream);
    this.adminPassword = adminPassword;
    this.sudoIsRequested = true;
  }

  public void run() {
    stopped = false;

    // on mac os x 10.5.x, when i run a 'sudo' command, i need to write
    // the admin password out immediately; that's why this code is
    // here.
    if (sudoIsRequested) {
      //doSleep(500);
      printWriter.println(adminPassword);
      printWriter.flush();
    }

    try (InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(isr)) {
      String line = null;
      while (! stopped && (line = bufferedReader.readLine()) != null) {
        outputBuffer.append(line + "\n");
        if (enableLog) {
          LOG.info("System Command Output: " + line);
        }
      }
    } catch (Exception ioe) {
      LOG.warn("Error reading process stream: " + ioe.getMessage());
    }
  }

  public void stopThread() {
    this.stopped = true;
  }

  private void doSleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      // ignore
    }
  }

  public void enableLog(boolean b) {
    enableLog = b;
  }

  public StringBuilder getOutputBuffer() {
    return outputBuffer;
  }

}








