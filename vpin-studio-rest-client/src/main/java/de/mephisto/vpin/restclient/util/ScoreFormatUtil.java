package de.mephisto.vpin.restclient.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class ScoreFormatUtil {
  private final static Logger LOG = LoggerFactory.getLogger(ScoreFormatUtil.class);

  private static DecimalFormatSymbols symbols;

  static {
    symbols = DecimalFormatSymbols.getInstance(Locale.getDefault());
  }

  public static String formatScore(String score) {
    try {
      score = cleanScore(score);
      DecimalFormat decimalFormat = new DecimalFormat("#.##", symbols);
      decimalFormat.setGroupingUsed(true);
      decimalFormat.setGroupingSize(3);
      return decimalFormat.format(Long.parseLong(score));
    }
    catch (NumberFormatException e) {
      LOG.error("Failed to read number from '" + score + "': " + e.getMessage());
      return "0";
    }
  }


  public static String cleanScore(String score) {
    return score.replace("\\.", "")
        .replace(",", "")
        .replace("\\?", "")
        .replace("\u00ff", "")
        .replace("\u00a0", "")
        .replace("\u202f", "")
        .replace("\ufffd", "")
        .replace(" ", "");
  }

}
