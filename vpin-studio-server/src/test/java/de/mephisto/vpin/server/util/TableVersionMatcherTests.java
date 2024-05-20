package de.mephisto.vpin.server.util;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import de.mephisto.vpin.server.vps.TableVersionMatcher;
import me.xdrop.fuzzywuzzy.FuzzySearch;

public class TableVersionMatcherTests {

  @Test
  public void testVersionDistance() {

System.out.println(FuzzySearch.weightedRatio("sixtoe", "vpw"));


    // check versions ignoring prefixes
    doTest("1.0.1", "1.0.1", 0);
    doTest("v1.0.1", "1.0.1", 0);
    doTest("v1.0.1", " version 1.0.1", 0);
    doTest("abcd 1.0.1", "1.0.1", 0);
    doTest("ef.gh 1.0.1", "1.0.1", 0);

    // When version contains a reference to VPX, ignore it
    doTest("1.0.1", "", 0.3);
    doTest("1.0.1", "VPX", 0.3);
    doTest("1.0.1", "VP9", 0.3);
    doTest("1.0.1", "VP10", 0.3);
    doTest("1.0.1", "VPX10", 0.3);
    doTest("1.0.1", "VPX 10.1", 0.3);
    doTest("1.0.1", "VPX/1.0.1", 0);
    doTest("1.0.1", "VP 10.1 - 1.0.1", 0);

    // Distance when versions are different, does not take depth into consideration
    doTest("1.0.1", "1.0.2", 0.5);
    doTest("1.0.1", "1.0.5", 2.0);
    doTest("1.0.1", "1.01", 0.5);
    doTest("2.0", "3.0", 0.5);
    doTest("2.0", "4.0", 1.0);
    doTest("1.2", "1.3.0", 0.5);
    // do not exceed 3.0
    doTest("1.2", "1.12", 3.0);

    // Different version depth with same beginning
    doTest("1.2", "1.2.0", 0.3);
    doTest("1.2", "v1.2.3", 0.3);
    doTest("1.2.0.5.0", "1.2.0", 0.3);
    doTest("1.0.1", "1", 0.3);
    doTest("1.1", "1", 0.3);


    // same version but suffix on one side
    doTest("1.0.1", "1.0.1a", 0.1);
    doTest("1.0.1", "1.0.1-snapshot", 0.1);
    doTest("1.0.1", "VP 10.1 - 1.0.1a", 0.1);
    // some versions are made of composition, consider first part, rest is suffix
    doTest("1.0-1.5", "1.0", 0.1);
    doTest("1.0", "1.0/1.5", 0.1);
    doTest("1.0:2.6", "1.0", 0.1);

    // distance with suffix 
    doTest("version 1.0-2.6", "1.1", 0.5);
    doTest("version 1.0a", "1.2", 1.0);
    
  }

  public void doTest(String s1, String s2, double expected) {

    double res = TableVersionMatcher.versionDistance(s1, s2);
    assertEquals(s1 + "<>" + s2 + " = " + res, expected, res, 0.01);

    res = TableVersionMatcher.versionDistance(s2, s1);
    assertEquals(s2 + "<>" + s1 + " = " + res, expected, res, 0.01);

  }

}


