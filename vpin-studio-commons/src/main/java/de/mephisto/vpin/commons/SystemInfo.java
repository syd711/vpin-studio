package de.mephisto.vpin.commons;


import de.mephisto.vpin.commons.utils.WinRegistry;
import de.mephisto.vpin.restclient.util.OSUtil;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SystemInfo {
  public static String RESOURCES = "./resources/";

  public final static String PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR = "pinupSystem.installationDir";
  public final static String PINBALLX_INSTALLATION_DIR_INST_DIR = "pinballX.installationDir";
  public final static String PINBALLY_INSTALLATION_DIR_INST_DIR = "pinballY.installationDir";
  public final static String STANDALONE_INSTALLATION_DIR_INST_DIR = "visualPinball.installationDir";
  public final static String ARCHIVE_TYPE = "archive.type";

  public final static String DOT_NET = "SOFTWARE\\Microsoft\\NET Framework Setup\\NDP";

  public File resolvePinUPSystemInstallationFolder() {
    if (OSUtil.isWindows()) {
      return SystemInfoWindows.INSTANCE.resolvePinUPSystemInstallationFolder();
    }
    return null;
  }

  public File resolvePinballXInstallationFolder() {
    if (OSUtil.isWindows()) {
      return SystemInfoWindows.INSTANCE.resolvePinballXInstallationFolder();
    }
    return null;
  }

  public File resolvePinballYInstallationFolder() {
    if (OSUtil.isWindows()) {
      return SystemInfoWindows.INSTANCE.resolvePinballYInstallationFolder();
    }
    return null;
  }

  public File resolveVpx64InstallFolder() {
    if (OSUtil.isWindows()) {
      return SystemInfoWindows.INSTANCE.resolveVpx64InstallFolder();
    }
    return null;
  }

  public File resolveVpx64Exe() {
    if (OSUtil.isWindows()) {
      return SystemInfoWindows.INSTANCE.resolveVpx64Exe();
    }
    return null;
  }

  public File resolveVpxExe() {
    if (OSUtil.isWindows()) {
      return SystemInfoWindows.INSTANCE.resolveVpxExe();
    }
    return null;
  }

  public File resolveVptInstallFolder() {
    if (OSUtil.isWindows()) {
      return SystemInfoWindows.INSTANCE.resolveVptInstallFolder();
    }
    return null;
  }

  public File resolveVptExe() {
    if (OSUtil.isWindows()) {
      return SystemInfoWindows.INSTANCE.resolveVptExe();
    }
    return null;
  }

  public File resolveFpInstallFolder() {
    if (OSUtil.isWindows()) {
      return SystemInfoWindows.INSTANCE.resolveFpInstallFolder();
    }
    return null;
  }

  public File resolveFpExe() {
    if (OSUtil.isWindows()) {
      return SystemInfoWindows.INSTANCE.resolveFpExe();
    }
    return null;
  }


  public File resolveBackglassServerFolder() {
    if (OSUtil.isWindows()) {
      return SystemInfoWindows.INSTANCE.resolveBackglassServerFolder();
    }
    return null;
  }

  //----------------------------------------------------------------------

  public boolean isDotNetInstalled() throws Exception {
    if (OSUtil.isWindows()) {
      List<String> localMachineKeys = WinRegistry.getLocalMachineKeys(DOT_NET);
      for (String localMachineKey : localMachineKeys) {
        if (localMachineKey.startsWith("v")) {
          if (isValidDotNetVersion(localMachineKey)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public boolean isValidDotNetVersion(String localMachineKey) {
    if (OSUtil.isWindows()) {
        String formatted = localMachineKey.replaceAll("\\.", "");
      String versionId = formatted.substring(1);
      if (versionId.length() == 1) {
        versionId = versionId + "0";
      }
      else if (versionId.length() > 2) {
        versionId = versionId.substring(0, 2);
      }

      int i = Integer.parseInt(versionId);
      return (i > 30);
    }
    return false;
  }

  public boolean isStickyKeysEnabled() {
    if (OSUtil.isWindows()) {
      Map<String, Object> currentUserValues = WinRegistry.getCurrentUserValues("Control Panel\\Accessibility\\StickyKeys\\");
      return currentUserValues.containsKey("Flags") && !currentUserValues.get("Flags").equals("506");
    }
    return false;
  }


  public void setStickyKeysEnabled(boolean b) {
    if (OSUtil.isWindows()) {
      //Map<String, Object> currentUserValues = getCurrentUserValues("Control Panel\\Accessibility\\StickyKeys\\");
      if (b) {
        WinRegistry.setUserValue("Control Panel\\Accessibility\\StickyKeys\\", "Flags", "510");
      }
      else {
        WinRegistry.setUserValue("Control Panel\\Accessibility\\StickyKeys\\", "Flags", "506");
      }
    }
  }

  //----------------------------------------------------------------------

  @NonNull
  public List<String> getCurrentUserKeys(String path) {
    if (OSUtil.isWindows()) {
      return WinRegistry.getCurrentUserKeys(path);
    }
    return Collections.emptyList();
  }

  @NonNull
  public Map<String, Object> getCurrentUserValues(String path) {
    if (OSUtil.isWindows()) {
      return WinRegistry.getCurrentUserValues(path);
    }
    return Collections.emptyMap();
  }

  public String readUserValue(String path, String key) {
    if (OSUtil.isWindows()) {
      return WinRegistry.readUserValue(path, key);
    }
    return null;
  }

  public void setUserValue(String path, String key, int value) {
    if (OSUtil.isWindows()) {
      WinRegistry.setUserIntValue(path, key, value);
    }
  }

  public void createUserKey(@NonNull String key) {
    if (OSUtil.isWindows()) {
      WinRegistry.createUserKey(key);
    }
  }

  public void deleteUserKey(@NonNull String key) {
    if (OSUtil.isWindows()) {
      WinRegistry.deleteUserKey(key);
    }
  }
}
