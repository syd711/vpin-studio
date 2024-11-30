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

  //Defender Exceptions => Computer\HKEY_LOCAL_MACHINE\
  public static final String DEFENDER_EXCLUSIONS = "SOFTWARE\\Microsoft\\Windows Defender\\Exclusions\\Paths";
  public static final String DOT_NET = "SOFTWARE\\Microsoft\\NET Framework Setup\\NDP";

  /**
   * Try "SOFTWARE\\Freeware\\Visual PinMame"
   */
  public static List<String> getCurrentUserKeys(@NonNull String path) {
    try {
      return Arrays.asList(Advapi32Util.registryGetKeys(WinReg.HKEY_CURRENT_USER, path));
    }
    catch (Exception e) {
      LOG.error("Failed to read registry folder " + path + ": " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  public static boolean isStickyKeysEnabled() {
    Map<String, Object> currentUserValues = WinRegistry.getCurrentUserValues("Control Panel\\Accessibility\\StickyKeys\\");
    return currentUserValues.containsKey("Flags") && !currentUserValues.get("Flags").equals("506");
  }


  public static void setStickyKeysEnabled(boolean b) {
    Map<String, Object> currentUserValues = WinRegistry.getCurrentUserValues("Control Panel\\Accessibility\\StickyKeys\\");
    if (b) {
      WinRegistry.setValue("Control Panel\\Accessibility\\StickyKeys\\", "Flags", "510");
    }
    else {
      WinRegistry.setValue("Control Panel\\Accessibility\\StickyKeys\\", "Flags", "506");
    }
    LOG.info("WinRegistry - Sticky keys enabled: " + b);
  }

  public static boolean isDotNetInstalled() throws Exception {
    List<String> localMachineKeys = getLocalMachineKeys(DOT_NET);
    LOG.info("Found .net registry entries: " + String.join(", ", localMachineKeys));
    for (String localMachineKey : localMachineKeys) {
      if (localMachineKey.startsWith("v")) {
        if (isValidDotNetVersion(localMachineKey)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isValidDotNetVersion(String localMachineKey) {
    String formatted = localMachineKey.replaceAll("\\.", "");
    String versionId = formatted.substring(1);
    if (versionId.length() == 1) {
      versionId = versionId + "0";
    }
    else if (versionId.length() > 2) {
      versionId = versionId.substring(0, 2);
    }

    int i = Integer.parseInt(versionId);
    if (i > 30) {
      LOG.info("Found .net framework " + localMachineKey);
      return true;
    }
    return false;
  }

  /**
   *
   */
  public static List<String> getLocalMachineKeys(@NonNull String path) {
    try {
      return Arrays.asList(Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, path));
    }
    catch (Exception e) {
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
    }
    catch (Exception e) {
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
    }
    catch (Exception e) {
      LOG.error("Failed to read registry key '" + path + "': " + e.getMessage());
    }
    return Collections.emptyMap();
  }

  public static Map<String, Object> getClassesValues(@NonNull String path) {
    try {
      return Advapi32Util.registryGetValues(WinReg.HKEY_CLASSES_ROOT, path);
    }
    catch (Exception e) {
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
    }
    catch (Exception e) {
      LOG.error("Failed to write value for path '" + path + "\\" + key + ": " + e.getMessage());
    }
  }

  /**
   *
   */
  public static void setValue(@NonNull String path, @NonNull String key, String value) {
    try {
      Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, path, key, value);
      LOG.info("Written " + path + "\\" + key + " => " + value);
    }
    catch (Exception e) {
      LOG.error("Failed to write value for path '" + path + "\\" + key + ": " + e.getMessage());
    }
  }

  public static void createKey(@NonNull String key) {
    try {
      Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, key);
      LOG.info("Created key " + key + "\\" + key);
    }
    catch (Exception e) {
      LOG.error("Failed to write value for path '" + key + "\\" + key + ": " + e.getMessage());
    }
  }

  public static void deleteKey(@NonNull String key) {
    try {
      Advapi32Util.registryDeleteKey(WinReg.HKEY_CURRENT_USER, key);
      LOG.info("Deleted key " + key + "\\" + key);
    }
    catch (Exception e) {
      LOG.info("Deletion failed for registry path '" + key + "\\" + key + ": " + e.getMessage());
    }
  }

  public static void deleteValue(@NonNull String key, @NonNull String value) {
    try {
      Advapi32Util.registryDeleteValue(WinReg.HKEY_CURRENT_USER, key, value);
      LOG.info("Deleted value key {}\\{}", key, value);
    }
    catch (Exception e) {
      LOG.info("Deletion failed for registry path '" + key + "\\" + value + ": " + e.getMessage());
    }
  }

  public static int getIntValue(@NonNull String path, @NonNull String key) {
    try {
      return Advapi32Util.registryGetIntValue(WinReg.HKEY_CURRENT_USER, path, key);
    }
    catch (Exception e) {
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