package de.mephisto.vpin.commons.utils;

import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class StringSimilarity {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static boolean isSimilarAtLeastToPercent(String s1, String s2, int percentage) {
    if (s1.equalsIgnoreCase(s2)) {
      return true;
    }

    double apply = getSimilarity(s1, s2);
    LOG.info("Calculated similarity between '" + s1 + "' and '" + s2 + "' to " + apply);
    return apply * 100 < (100 - percentage);
  }

  public static double getSimilarity(String s1, String s2) {
    JaroWinklerDistance jwd = new JaroWinklerDistance();
    return jwd.apply(s1, s2);
  }
}
