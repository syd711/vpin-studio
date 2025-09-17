package de.mephisto.vpin.restclient.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class can be used to execute a system command from a Java application.
 * See the documentation for the public methods of this class for more
 * information.
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
public class SystemCommandExecutor {
  private final static Logger LOG = LoggerFactory.getLogger(SystemCommandExecutor.class);

  private List<String> commandInformation;
  private String adminPassword;
  private ThreadedStreamHandler inputStreamHandler;
  private ThreadedStreamHandler errorStreamHandler;
  private Process process;
  private boolean enableLogging = false;
  private String commandError;
  private File dir;
  private boolean ignoreError;

  /**
   * Pass in the system command you want to run as a List of Strings, as shown here:
   * <p/>
   * List<String> commands = new ArrayList<String>();
   * commands.add("/sbin/ping");
   * commands.add("-c");
   * commands.add("5");
   * commands.add("www.google.com");
   * SystemCommandExecutor commandExecutor = new SystemCommandExecutor(commands);
   * commandExecutor.executeCommand();
   * <p/>
   * Note: I've removed the other constructor that was here to support executing
   * the sudo command. I'll add that back in when I get the sudo command
   * working to the point where it won't hang when the given password is
   * wrong.
   *
   * @param commandInformation The command you want to run.
   */
  public SystemCommandExecutor(final List<String> commandInformation) {
    this(commandInformation, true);
  }

  public SystemCommandExecutor(final List<String> commandInformation, boolean prependCmd) {
    if (commandInformation == null) {
      throw new NullPointerException("The commandInformation is required.");
    }
    this.commandInformation = new ArrayList<>(commandInformation);
    this.adminPassword = null;

    if (prependCmd && !commandInformation.get(0).equalsIgnoreCase("cmd.exe")) {
      this.commandInformation.add(0, "/c");
      this.commandInformation.add(0, "cmd.exe");
    }
  }

  public void setIgnoreError(boolean ignoreError) {
    this.ignoreError = ignoreError;
  }

  public void enableLogging(boolean b) {
    this.enableLogging = b;
  }

  public void executeCommandAsync() {
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          execute();
        }
        catch (Exception e) {
          LOG.error("Failed to execute command " + Joiner.on(" ").join(commandInformation) + ": " + e.getMessage(), e);
        }
      }
    };
    t.setName("Async. Executor for " + Joiner.on(" ").join(commandInformation));
    t.start();
  }

  public int executeCommand() throws IOException, InterruptedException {
    return execute();
  }

  private int execute() throws IOException, InterruptedException {
    int exitValue = -99;

    try {
      LOG.info("System Command: " + (this.dir != null ? dir.getAbsolutePath() : "") + "> " + String.join(" ", commandInformation));

      ProcessBuilder pb = new ProcessBuilder(commandInformation);

      if (dir != null) {
        pb.directory(dir);
      }

      process = pb.start();

      // you need this if you're going to write something to the command's input stream
      // (such as when invoking the 'sudo' command, and it prompts you for a password).
      OutputStream stdOutput = process.getOutputStream();

      // i'm currently doing these on a separate line here in case i need to set them to null
      // to get the threads to stop.
      // see http://java.sun.com/j2se/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html
      InputStream inputStream = process.getInputStream();
      InputStream errorStream = process.getErrorStream();

      // these need to run as java threads to get the standard output and error from the command.
      // the inputstream handler gets a reference to our stdOutput in case we need to write
      // something to it, such as with the sudo command
      inputStreamHandler = new ThreadedStreamHandler(Joiner.on(" ").join(commandInformation), inputStream, stdOutput, adminPassword);
      inputStreamHandler.enableLog(enableLogging);
      errorStreamHandler = new ThreadedStreamHandler(Joiner.on(" ").join(commandInformation), errorStream);
      errorStreamHandler.enableLog(enableLogging);

      inputStreamHandler.start();
      errorStreamHandler.start();

      exitValue = process.waitFor();

      inputStreamHandler.stopThread();
      errorStreamHandler.stopThread();
    }
    catch (Exception e) {
      if (ignoreError) {
        LOG.info("Failed to execute system command '" + Joiner.on(" ").join(commandInformation) + "': exit code " + exitValue + ", " + e.getMessage());
      }
      else {
        LOG.error("Failed to execute system command '" + Joiner.on(" ").join(commandInformation) + "': exit code " + exitValue + ", " + e.getMessage());
        throw e;
      }
    }
    return -1;
  }

  public void killProcess() {
    process.destroyForcibly();
  }

  /**
   * Get the standard output (stdout) from the command you just exec'd.
   */
  public StringBuilder getStandardOutputFromCommand() {
    if (inputStreamHandler != null) {
      return inputStreamHandler.getOutputBuffer();
    }
    return new StringBuilder();
  }

  /**
   * Get the standard error (stderr) from the command you just exec'd.
   */
  public StringBuilder getStandardErrorFromCommand() {
    if (commandError != null) {
      return new StringBuilder(commandError);
    }

    if (errorStreamHandler != null) {
      return errorStreamHandler.getOutputBuffer();
    }

    return new StringBuilder();
  }

  public void setDir(File dir) {
    this.dir = dir;
  }
}
