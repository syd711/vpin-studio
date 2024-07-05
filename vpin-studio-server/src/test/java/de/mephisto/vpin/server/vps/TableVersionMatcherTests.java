package de.mephisto.vpin.server.vps;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class TableVersionMatcherTests {

  @Test
  public void testVersionDistance() {

    TableVersionMatcher matcher = new TableVersionMatcher();

    // check versions ignoring prefixes
    doTest(matcher, "1.0.1", "1.0.1", 0);
    doTest(matcher, "v1.0.1", "1.0.1", 0);
    doTest(matcher, "v1.0.1", " version 1.0.1", 0);
    doTest(matcher, "abcd 1.0.1", "1.0.1", 0);
    doTest(matcher, "ef.gh 1.0.1", "1.0.1", 0);

    // When version contains a reference to VPX, ignore it
    doTest(matcher, "1.0.1", "", 0.3);
    doTest(matcher, "1.0.1", "VPX", 0.3);
    doTest(matcher, "1.0.1", "VP9", 0.3);
    doTest(matcher, "1.0.1", "VP10", 0.3);
    doTest(matcher, "1.0.1", "VPX10", 0.3);
    doTest(matcher, "1.0.1", "VPX 10.1", 0.3);
    doTest(matcher, "1.0.1", "VPX/1.0.1", 0);
    doTest(matcher, "1.0.1", "VP 10.1 - 1.0.1", 0);

    // Distance when versions are different, does not take depth into consideration
    doTest(matcher, "1.0.1", "1.0.2", 0.5);
    doTest(matcher, "1.0.1", "1.0.5", 2.0);
    doTest(matcher, "1.0.1", "1.01", 0.5);
    doTest(matcher, "2.0", "3.0", 0.5);
    doTest(matcher, "2.0", "4.0", 1.0);
    doTest(matcher, "1.2", "1.3.0", 0.5);
    // do not exceed 3.0
    doTest(matcher, "1.2", "1.12", 3.0);

    // Different version depth with same beginning
    doTest(matcher, "1.2", "1.2.0", 0.3);
    doTest(matcher, "1.2", "v1.2.3", 0.3);
    doTest(matcher, "1.2.0.5.0", "1.2.0", 0.3);
    doTest(matcher, "1.0.1", "1", 0.3);
    doTest(matcher, "1.1", "1", 0.3);


    // same version but suffix on one side
    doTest(matcher, "1.0.1", "1.0.1a", 0.1);
    doTest(matcher, "1.0.1", "1.0.1-snapshot", 0.1);
    doTest(matcher, "1.0.1", "VP 10.1 - 1.0.1a", 0.1);
    // some versions are made of composition, consider first part, rest is suffix
    doTest(matcher, "1.0-1.5", "1.0", 0.1);
    doTest(matcher, "1.0", "1.0/1.5", 0.1);
    doTest(matcher, "1.0:2.6", "1.0", 0.1);

    // distance with suffix 
    doTest(matcher, "version 1.0-2.6", "1.1", 0.5);
    doTest(matcher, "version 1.0a", "1.2", 1.0);
    
  }

  public void doTest(TableVersionMatcher matcher, String s1, String s2, double expected) {

    double res = matcher.versionDistance(s1, s2);
    assertEquals(s1 + "<>" + s2 + " = " + res, expected, res, 0.01);

    res = matcher.versionDistance(s2, s1);
    assertEquals(s2 + "<>" + s1 + " = " + res, expected, res, 0.01);

  }

}


