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
    String baseName = "abc (2001)";
    Pattern plainMatcher  = Pattern.compile(Pattern.quote(baseName) + "\\d{0,2}\\.[a-zA-Z0-9]*");
    Pattern screenMatcher  = Pattern.compile(Pattern.quote(baseName) + "\\d{0,2}\\(.*\\)\\.[a-zA-Z0-9]*");

    List<String> matches = Arrays.asList(baseName + ".apng", baseName + "01.png", baseName + "99.mp3");
    for (String name : matches) {
      assertTrue(plainMatcher.matcher(name).matches(), "test failed for '" + name + "'");
    }

    List<String> nonMatches = Arrays.asList(baseName + "d.png", baseName + "d01.apng", baseName+ "d-99.png", baseName + "-(9).mp3");
    for (String name : nonMatches) {
      assertFalse(plainMatcher.matcher(name).matches());
    }

    List<String> screenMatches = Arrays.asList(baseName+ "(Screen).png", baseName + "(Screen 3).apng", baseName+ "01(SCREEN3).mp4", baseName+ "99(Screen 3).png", baseName+ "99(Screen 3).mp4");
    for (String name : screenMatches) {
      assertTrue(FilenameUtils.getBaseName(name).equalsIgnoreCase(baseName) || screenMatcher.matcher(name).matches());
    }


    List<String> nonMatchesScreen = Arrays.asList(baseName + "d(Screen).png",  baseName+ "cd01(Screen 3).apng", baseName+ "d-99 (Screen 3).png", baseName + "cd-99 (Screen 3).mp4");
    for (String name : nonMatchesScreen) {
      assertFalse(screenMatcher.matcher(name).matches());
    }
  }
}
