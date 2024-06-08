package de.mephisto.vpin.restclient.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;

public class ScoreFormatUtil {
  private final static Logger LOG = LoggerFactory.getLogger(ScoreFormatUtil.class);

  public static String formatScore(String score) {
    try {
      score = score.replaceAll("\\.", "").replaceAll(",", "");
      DecimalFormat decimalFormat = new DecimalFormat("#.##");
      decimalFormat.setGroupingUsed(true);
      decimalFormat.setGroupingSize(3);
      return decimalFormat.format(Long.parseLong(score));
    } catch (NumberFormatException e) {
      LOG.error("Failed to read number from '" +score + "': " + e.getMessage());
      return "0";
    }
  }

}
