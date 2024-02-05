package de.mephisto.vpin.server.games;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AssetLookupTest {

  @Test
  public void testMediaLookup() {
    String baseName = "abc";
    Pattern plainMatcher  = Pattern.compile(baseName + "\\d{0,2}\\.[a-zA-Z]*");
    Pattern screenMatcher  = Pattern.compile(baseName + "\\d{0,2}\\(.*\\)\\.[a-zA-Z]*");

    List<String> matches = Arrays.asList("abc.png", "abc.apng", "abc01.png", "abc99.png");
    for (String name : matches) {
      assertTrue(plainMatcher.matcher(name).matches());
    }

    List<String> nonMatches = Arrays.asList("abcd.png", "abcd01.apng", "abcd-99.png");
    for (String name : nonMatches) {
      assertFalse(plainMatcher.matcher(name).matches());
    }

    List<String> screenMatches = Arrays.asList("abc(Screen).png", "abc(Screen 3).apng", "abc01(Screen 3).png", "abc99(Screen 3).png");
    for (String name : screenMatches) {
      assertTrue(FilenameUtils.getBaseName(name).equalsIgnoreCase(baseName) || screenMatcher.matcher(name).matches());
    }


    List<String> nonMatchesScreen = Arrays.asList("abcd(Screen).png", "abcd01(Screen 3).apng", "abcd-99 (Screen 3).png");
    for (String name : nonMatchesScreen) {
      assertFalse(screenMatcher.matcher(name).matches());
    }

  }
}
