package de.mephisto.vpin.server.util;

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

  //Defender Exceptions => Computer\HKEY_LOCAL_MACHINE\
  public static final String DEFENDER_EXCLUSIONS = "SOFTWARE\\Microsoft\\Windows Defender\\Exclusions\\Paths";

  /**
   * Try "SOFTWARE\\Freeware\\Visual PinMame"
   */
  public static List<String> getCurrentUserKeys(@NonNull String path) {
    try {
      return Arrays.asList(Advapi32Util.registryGetKeys(WinReg.HKEY_CURRENT_USER, path));
    } catch (Exception e) {
      LOG.error("Failed to read registry folder " + path + ": " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }
  /**
   * Try "SOFTWARE\\Freeware\\Visual PinMame"
   */
  public static List<String> getClassesKeys(@NonNull String path) {
    try {
      return Arrays.asList(Advapi32Util.registryGetKeys(WinReg.HKEY_CLASSES_ROOT, path));
    } catch (Exception e) {
      LOG.error("Failed to read registry folder " + path + ": " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  /**
   * Trr SOFTWARE\\Freeware\\Visual PinMame\\" + s + "\\"
   */
  public static Map<String, Object> getCurrentUserValues(@NonNull String path) {
    try {
      return Advapi32Util.registryGetValues(WinReg.HKEY_CURRENT_USER, path);
    } catch (Exception e) {
      LOG.error("Failed to read registry key '" + path + "': " + e.getMessage());
    }
    return Collections.emptyMap();
  }

  public static Map<String, Object> getClassesValues(@NonNull String path) {
    try {
      return Advapi32Util.registryGetValues(WinReg.HKEY_CLASSES_ROOT, path);
    } catch (Exception e) {
      LOG.error("Failed to read registry key '" + path + "': " + e.getMessage());
    }
    return Collections.emptyMap();
  }

  /**
   *
   */
  public static void setIntValue(@NonNull String path, @NonNull String key, int value) {
    try {
      Advapi32Util.registrySetIntValue(WinReg.HKEY_CURRENT_USER, path, key, value);
      LOG.info("Written " + path + "\\" + key + " => " + value);
    } catch (Exception e) {
      LOG.error("Failed to write value for path '" + path + "\\" + key + ": " + e.getMessage());
    }
  }

  public static void createKey(@NonNull String key) {
    try {
      Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, key);
      LOG.info("Created key " + key + "\\" + key);
    } catch (Exception e) {
      LOG.error("Failed to write value for path '" + key + "\\" + key + ": " + e.getMessage());
    }
  }

  public static void deleteKey(@NonNull String key) {
    try {
      Advapi32Util.registryDeleteKey(WinReg.HKEY_CURRENT_USER, key);
      LOG.info("Deleted key " + key + "\\" + key);
    } catch (Exception e) {
      LOG.info("Deletion failed for registry path '" + key + "\\" + key + ": " + e.getMessage());
    }
  }

  public static int getIntValue(@NonNull String path, @NonNull String key) {
    try {
      return Advapi32Util.registryGetIntValue(WinReg.HKEY_CURRENT_USER, path, key);
    } catch (Exception e) {
      LOG.error("Failed to read int value from " + path + key + ": " + e.getMessage());
    }
    return 0;
  }

  public static void main(String[] args) throws Exception {
//    Advapi32Util.registrySetIntValue(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\\\Freeware\\\\Visual PinMame\\default", "sound", 0);
//    System.out.println(Advapi32Util.registryGetIntValue(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\\\Freeware\\\\Visual PinMame\\default", "sound"));
    WinRegistry.createKey("SOFTWARE\\\\Freeware\\\\Visual PinMame\\bubu");
  }
}