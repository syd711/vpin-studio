package de.mephisto.vpin.restclient.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScoreFormatUtilTest {

  @Test
  public void testLocalizedScore() {

    String s = ScoreFormatUtil.formatScore("123456789");
    assertTrue(s.contains(".") || s.contains(",") || s.contains(String.valueOf((char) 160)));
  }
}
