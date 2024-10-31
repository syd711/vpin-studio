package de.mephisto.vpin.restclient.util;

public class OSUtil {

  public static boolean isWindows() {
    String os = System.getProperty("os.name").toLowerCase();
    return os.contains("windows");
  }

  public static boolean isMac() {
    String os = System.getProperty("os.name").toLowerCase();
    return os.contains("mac") || os.contains("darwin");
  }
}
