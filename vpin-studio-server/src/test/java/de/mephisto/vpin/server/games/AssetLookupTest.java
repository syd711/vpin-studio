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
    Pattern p  = Pattern.compile(baseName + "\\d{2,2}\\.[a-zA-Z]*");

    List<String> matches = Arrays.asList("abc.png", "abc.apng", "abc01.png", "abc99.png");
    for (String name : matches) {
      assertTrue(FilenameUtils.getBaseName(name).equalsIgnoreCase(baseName) || p.matcher(name).matches());
    }
    List<String> nonMatches = Arrays.asList("abcd.png", "abcd01.apng", "abcd-99.png");
    for (String name : nonMatches) {
      assertFalse(FilenameUtils.getBaseName(name).equalsIgnoreCase(baseName) || p.matcher(name).matches());
    }
  }
}
