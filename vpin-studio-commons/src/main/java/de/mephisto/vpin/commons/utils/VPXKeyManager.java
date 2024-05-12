package de.mephisto.vpin.commons.utils;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class VPXKeyManager {
  private final static Logger LOG = LoggerFactory.getLogger(VPXKeyManager.class);

  public final static String LFlipKey = "LFlipKey";
  public final static String RFlipKey = "RFlipKey";
  public final static String StartGameKey = "StartGameKey";

  private final static File VPINBALL_INI = new File(System.getProperty("user.home"), "AppData\\Roaming\\VPinballX\\VPinballX.ini");

  private static VPXKeyManager INSTANCE = null;
  private INIConfiguration iniConfiguration;

  private static Map<Integer,Integer> directX2Native = new HashMap<>();

  static {
    directX2Native.put(29, 162); //Ctrl Left
    directX2Native.put(157, 163); //Ctrl Right
    directX2Native.put(42, 160); //Shift Left
    directX2Native.put(54, 161); //Shift Right
  }

  private VPXKeyManager() {

  }

  public static VPXKeyManager getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new VPXKeyManager();
      INSTANCE.reloadKeyBinding();
    }
    return INSTANCE;
  }

  public int getBinding(String name) {
    try {
      if (iniConfiguration != null) {
        SubnodeConfiguration player = iniConfiguration.getSection("Player");
        int binding = player.getInt(name);
        return getMappedValue(binding);
      }
    } catch (Exception e) {
      LOG.error("Failed to read VPX key binding for '" + name + "': " + e.getMessage(), e);
    }
    return -1;
  }


  public int getMappedValue(int value) {
    if(directX2Native.containsKey(value)) {
      return directX2Native.get(value);
    }
    return value;
  }

  public void reloadKeyBinding() {
    try {
      if (VPINBALL_INI.exists()) {
        iniConfiguration = new INIConfiguration();
        iniConfiguration.setCommentLeadingCharsUsedInInput(";");
        iniConfiguration.setSeparatorUsedInOutput("=");
        iniConfiguration.setSeparatorUsedInInput("=");

        FileReader fileReader = new FileReader(VPINBALL_INI);
        iniConfiguration.read(fileReader);
      }
      else {
        LOG.warn("Unable to find VPinbal.ini: " + VPINBALL_INI.getAbsolutePath());
      }
    } catch (Exception e) {
      LOG.info("Error reading VPX key bindings: " + e.getMessage(), e);
    }
  }
}
