package de.mephisto.vpin.commons.utils;

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
    String cmd = "@echo off\n" +
        "for /f %%V in ('\n" +
        "  powershell -NoProfile -Command ^\n" +
        "    \"Add-Type -AssemblyName presentationcore; ^\n" +
        "     [math]::Round([System.Windows.Media.SystemParameters]::Volume * 100)\"\n" +
        "') do (\n" +
        "  echo System volume: %%V%%\n" +
        ")\n";
    try {
      File batFile = FileUtils.writeBatch("getSystemVolume.bat", cmd);
      List<String> strings = Arrays.asList(batFile.getName());
      SystemCommandExecutor executor = new SystemCommandExecutor(strings);
      executor.setDir(batFile.getParentFile());
      executor.executeCommand();

      System.out.println(executor.getStandardOutputFromCommand());
    }
    catch (Exception e) {
      //ignore
    }
    return -1;
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
    NirCmd.getSystemVolume();
  }
}
