package de.mephisto.vpin.restclient.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScoreFormatUtilTest {

  @Test
  public void testLocalizedScore() {
    String s = ScoreFormatUtil.formatScore("123456789");
    assertEquals(s, "123.456.789");
  }
}
