package de.mephisto.vpin.commons.utils;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WinRegistry {
  private final static Logger LOG = LoggerFactory.getLogger(WinRegistry.class);

  /**
   * Try "SOFTWARE\\Freeware\\Visual PinMame"
   */
  @NonNull
  public static List<String> getCurrentUserKeys(@NonNull String path) {
    try {
      return Arrays.asList(Advapi32Util.registryGetKeys(WinReg.HKEY_CURRENT_USER, path));
    }
    catch (Exception e) {
      LOG.error("Failed to read registry folder HKCU\\" + path + ": " + e.getMessage());
    }
    return Collections.emptyList();
  }

  /**
   *
   */
  @NonNull
  public static List<String> getLocalMachineKeys(@NonNull String path) {
    try {
      return Arrays.asList(Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, path));
    }
    catch (Exception e) {
      LOG.error("Failed to read registry folder " + path + ": " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  @NonNull
  public static Map<String, Object> getClassesValues(@NonNull String path) {
    try {
      return Advapi32Util.registryGetValues(WinReg.HKEY_CLASSES_ROOT, path);
    }
    catch (Exception e) {
      LOG.error("Failed to read registry key '" + path + "': " + e.getMessage());
    }
    return Collections.emptyMap();
  }

  @NonNull
  public static Map<String, Object> getCurrentUserValues(@NonNull String path) {
    try {
      return Advapi32Util.registryGetValues(WinReg.HKEY_CURRENT_USER, path);
    }
    catch (Exception e) {
      LOG.error("Failed to read registry key '" + path + "': " + e.getMessage());
    }
    return Collections.emptyMap();
  }

  public static String readUserValue(String path, String key) {
    try {
      return Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, path, key);
    }
    catch (Exception e) {
      LOG.error("Failed to read registry key '" + path + "': " + e.getMessage());
    }
    return null;
  }

  public static void setUserIntValue(@NonNull String path, @NonNull String key, int value) {
    try {
      Advapi32Util.registrySetIntValue(WinReg.HKEY_CURRENT_USER, path, key, value);
      LOG.info("Written " + path + "\\" + key + " => " + value);
    }
    catch (Exception e) {
      LOG.error("Failed to write value for path '" + path + "\\" + key + ": " + e.getMessage());
    }
  }

  public static void setUserValue(@NonNull String path, @NonNull String key, String value) {
    try {
      Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, path, key, value);
      LOG.info("Written " + path + "\\" + key + " => " + value);
    }
    catch (Exception e) {
      LOG.error("Failed to write value for path '" + path + "\\" + key + ": " + e.getMessage());
    }
  }

  public static void createUserKey(@NonNull String key) {
    try {
      Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, key);
      LOG.info("Created key " + key + "\\" + key);
    }
    catch (Exception e) {
      LOG.error("Failed to write value for path '" + key + "\\" + key + ": " + e.getMessage());
    }
  }

  public static void deleteUserKey(@NonNull String key) {
    try {
      Advapi32Util.registryDeleteKey(WinReg.HKEY_CURRENT_USER, key);
      LOG.info("Deleted key " + key + "\\" + key);
    }
    catch (Exception e) {
      LOG.info("Deletion failed for registry path '" + key + "\\" + key + ": " + e.getMessage());
    }
  }
}