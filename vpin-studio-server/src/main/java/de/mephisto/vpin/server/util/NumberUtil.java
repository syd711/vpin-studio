package de.mephisto.vpin.server.util;

import org.apache.commons.lang3.StringUtils;

public class NumberUtil {

  public static int parseIntSafe(String value) {
    return parseIntSafe(value, 0);
  }

  public static int parseIntSafe(String value, int defaultValue) {
    try {
      if (!StringUtils.isEmpty(value)) {
        value = value.replaceAll("@", "").trim();
        return Integer.parseInt(value);
      }
    }
    catch (NumberFormatException e) {
      //ignore
    }
    return defaultValue;
  }
}
