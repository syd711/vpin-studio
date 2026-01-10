package de.mephisto.vpin.commons.utils;

import com.sun.jna.ptr.FloatByReference;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

public class NirCmd {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static void focusWindow(String title) {
    List<String> commands = Arrays.asList("nircmd.exe", "win", "activate", "ititle", "\"" + title + "\"");
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(new File("./resources"));
    executor.executeCommandAsync();
  }

  public static void setTopMost(String title) {
    List<String> commands = Arrays.asList("nircmd.exe", "win", "settopmost", "ititle", "\"" + title + "\"", "1");
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(new File("./resources"));
    executor.executeCommandAsync();
  }

  public static void muteSystem(boolean mute) {
    List<String> commands = Arrays.asList("nircmd.exe", "mutesysvolume", mute ? "1" : "0");
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(new File("./resources"));
    executor.executeCommandAsync();
  }

  public static int getSystemVolume() {
    // This is a simplified example - full implementation requires
    // proper COM interface definitions
    FloatByReference volume = new FloatByReference();
    // You'd need to properly define COM interfaces for Core Audio API
    return (int) volume.getValue();
  }

  public static void setVolume(int volume) {
    int vol = 65535 * volume / 100;
    List<String> commands = Arrays.asList("nircmd.exe", "setvolume", "0", String.valueOf(vol), String.valueOf(vol));
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(new File("./resources"));
    executor.executeCommandAsync();
  }

  public static void setTaskBarVisible(boolean visible) {
    List<String> commands = Arrays.asList("nircmd.exe", "win", "hide", "class", "Shell_TrayWnd");
    if (visible) {
      commands = Arrays.asList("nircmd.exe", "win", "show", "class", "Shell_TrayWnd");
    }
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(new File("./resources"));
    executor.executeCommandAsync();
  }

  public static void main(String[] args) {
//    NirCmd.setTopMost("Chrome");
//    NirCmd.setTopMost("Edge");
    System.out.println(NirCmd.getSystemVolume());
  }
}
