package de.mephisto.vpin.server.ini;

import org.apache.commons.configuration2.SubnodeConfiguration;

public class IniUtil {
  public static int safeGet(SubnodeConfiguration conf, String key) {
    return safeGet(conf, key, 0);
  }

  public static int safeGet(SubnodeConfiguration conf, String key, int defValue) {
    if (conf != null && !conf.isEmpty() && conf.containsKey(key)) {
      try {
        double value = conf.getDouble(key);
        return (int) value;
      }
      catch (Exception e) {
        return defValue;
      }
    }
    return defValue;
  }

  public static boolean safeGetBoolean(SubnodeConfiguration conf, String key, boolean defValue) {
    return conf != null && !conf.isEmpty() && conf.containsKey(key) ? conf.getBoolean(key) : defValue;
  }
}
