package de.mephisto.vpin.commons.utils;

import de.mephisto.vpin.commons.fx.pausemenu.ChromeLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class NirCmd {
  private final static Logger LOG = LoggerFactory.getLogger(NirCmd.class);

  public static void focusWindow(String title) {
    List<String> commands = Arrays.asList("nircmd.exe", "win", "activate",  "ititle", "\"" + title + "\"");
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(new File("./resources"));
    executor.executeCommandAsync();
    LOG.info("NirCmd: " + String.join(" ", commands));
  }

  public static void setTopMost(String title) {
    List<String> commands = Arrays.asList("nircmd.exe",  "win", "settopmost", "ititle", "\"" + title + "\"", "1");
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(new File("./resources"));
    executor.executeCommandAsync();
    LOG.info("NirCmd: " + String.join(" ", commands));
  }
}
