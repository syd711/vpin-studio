package de.mephisto.vpin.server.nvrams.parser;

import java.util.Locale;

/**
 * Utility methods for BCD (Binary Coded Decimal) conversions.
 */
public class BcdUtils {

  public static int bcdNibbleToInt(int bcdValue) {
    if (bcdValue < 0x0 || bcdValue > 0xF) {
      throw new IllegalArgumentException(String.format("Value 0x%X exceeds 4 bits", bcdValue));
    }
    return (bcdValue > 9) ? 0 : bcdValue;
  }

  public static int bcdByteToInt(int bcdValue) {
    if (bcdValue < 0x0 || bcdValue > 0xFF) {
      throw new IllegalArgumentException(String.format("Value 0x%X exceeds 8 bits", bcdValue));
    }
    return bcdNibbleToInt(bcdValue >> 4) * 10 + bcdNibbleToInt(bcdValue & 0x0F);
  }

  public static int intByteToBcd(int intValue) {
    if (intValue < 0 || intValue > 99) {
      throw new IllegalArgumentException(String.format("Cannot convert %d to BCD byte", intValue));
    }
    return (intValue / 10) * 16 + (intValue % 10);
  }

  

  public static int toInt(Object v) {
    if (v instanceof Integer) return (Integer) v;
    if (v instanceof Long) return ((Long) v).intValue();
    if (v instanceof String) {
      String s = (String) v;
      if (s.startsWith("0x") || s.startsWith("0X")) {
        return Integer.parseInt(s.substring(2), 16);
      } else if (s.startsWith("0") && s.length() > 1) {
        return Integer.parseInt(s, 8);
      } else {
        return Integer.parseInt(s);
      }
    }
    throw new IllegalArgumentException("Cannot convert to int: " + v);
  }

  public static String formatNumber(long number, Locale locale) {
    return String.format(locale, "%,d", number);
  }
}
