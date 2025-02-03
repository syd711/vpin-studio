package de.mephisto.vpin.commons.utils;

import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class VPXKeyManager {
  private final static Logger LOG = LoggerFactory.getLogger(VPXKeyManager.class);

  public final static String LFlipKey = "LFlipKey";
  public final static String RFlipKey = "RFlipKey";
  public final static String StartGameKey = "StartGameKey";

  private Configuration playerConfiguration;

  private static Map<Integer,Integer> directX2Native = new HashMap<>();

  static {
    directX2Native.put(29, 162); //Ctrl Left
    directX2Native.put(157, 163); //Ctrl Right
    directX2Native.put(42, 160); //Shift Left
    directX2Native.put(54, 161); //Shift Right
  }

  public VPXKeyManager(Configuration playerConfiguration) {
    this.playerConfiguration = playerConfiguration;
  }

  public int getBinding(String name) {
    try {
      if (playerConfiguration != null) {
        int binding = playerConfiguration.getInt(name);
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
}
