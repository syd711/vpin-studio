package de.mephisto.vpin.commons.utils;

import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringSimilarity {
  private final static Logger LOG = LoggerFactory.getLogger(StringSimilarity.class);

  public static boolean isSimilarAtLeastToPercent(String s1, String s2, int percentage) {
    double apply = getSimilarity(s1, s2);
    LOG.info("Calculated similarity between '" + s1 + "' and '" + s2 + "' to " + apply);
    return apply * 100 < (100-percentage);
  }

  public static double getSimilarity(String s1, String s2) {
    JaroWinklerDistance jwd = new JaroWinklerDistance();
    return jwd.apply(s1, s2);
  }
}
