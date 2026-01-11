package de.mephisto.vpin.restclient.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

public class ScoreFormatUtilTest {

  @Test
  public void testLocalizedScore() {
    String s = ScoreFormatUtil.formatScore(123456789);
    assertTrue(s.contains(".") || s.contains(",") || s.contains(" "));

    assertEquals("123,456,789", ScoreFormatUtil.formatScore(123456789, Locale.US));
    assertEquals("123.456.789", ScoreFormatUtil.formatScore(123456789, Locale.GERMANY));
    assertEquals("123 456 789", ScoreFormatUtil.formatScore(123456789, Locale.FRANCE));
  }
}
