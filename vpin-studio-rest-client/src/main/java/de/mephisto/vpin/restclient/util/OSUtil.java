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

  public static boolean isLinux() {
    String os = System.getProperty("os.name").toLowerCase();
    return os.contains("nix") || os.contains("nux") || os.contains("aix");
  }
}
