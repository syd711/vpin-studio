package de.mephisto.vpin.connectors.vps.matcher;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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
    doTest(matcher, "1.0.1", "VPX10.1", 0.3);
    doTest(matcher, "1.0.1", "VPX/1.0.1", 0);
    doTest(matcher, "1.0.1", "VPX/1.0.3", 0.02, 1.0);
    doTest(matcher, "1.0.1", "VP10.1 - 1.0.1", 0);
    doTest(matcher, "1.0.1", "VP10.1  1.0.4", 0.03, 1.0);

    // Distance when versions are different, does not take depth into consideration
    doTest(matcher, "1.0.1", "1.0.2", 0.01, 1.0);
    doTest(matcher, "1.0.1", "1.0.5", 0.04, 1.0);
    doTest(matcher, "1.0.1", "1.01", 0.1, 2.0);
    doTest(matcher, "2.0", "3.0", 0.3, 3.0);
    doTest(matcher, "2.0", "4.0", 0.3, 3.0);
    doTest(matcher, "1.2", "1.3", 0.1, 2.0);
    doTest(matcher, "1.2", "1.5", 0.3, 2.0);
    doTest(matcher, "1.2", "1.6", 0.3, 2.0);
    // do not exceed 2.0
    doTest(matcher, "1.2", "1.12", 0.3, 2.0);

    // Different version depth with same beginning
    doTest(matcher, "1.2", "1.2.0", 0.003);
    doTest(matcher, "1.2", "v1.2.3", 0.003);
    doTest(matcher, "1.2.0.5.0", "1.2.0", 0.0003);
    doTest(matcher, "1.0.1", "1", 0.03);
    doTest(matcher, "1.1", "1", 0.03);


    // same version but suffix on one side
    doTest(matcher, "1.0.1a", "1.0.1", 0.2);
    doTest(matcher, "1.0.1", "1.0.1-snapshot", 0.2);
    doTest(matcher, "1.0.1", "VP10.1 - 1.0.1a", 0.2);
    // some versions are made of composition, consider first part, rest is suffix
    doTest(matcher, "1.0-1.5", "1.0", 0.2);
    doTest(matcher, "1.0-1.3", "1.0-1.5", 0.05, 0.5);
    doTest(matcher, "1.0-1.5", "1.0/1.5", 0);
    doTest(matcher, "1.0-1.5", "1.0/1.6", 0.025, 0.5);
    doTest(matcher, "1.0:2.6", "1.0", 0.2);
    doTest(matcher, "1.0:2.6", "1.0:3.8", 0.075, 0.75);

    doTest(matcher, "1.0", "1.0d", 0.2);
    doTest(matcher, "1.0b", "1.0d", 0.1, 1.0);



    // distance with suffix 
    doTest(matcher, "version 1.0-2.6", "1.2", 0.2, 2.0);
    doTest(matcher, "version 1.0a", "1.2", 0.2, 2.0);
    
  }

  /**
   * Order matter in argument call, s1 < s2
   * when not,  
   */
  public void doTest(TableVersionMatcher matcher, String s1, String s2, double expected) {
    doTest(matcher, s1, s2, expected, expected);
  }
  public void doTest(TableVersionMatcher matcher, String s1, String s2, double expected1, double expected2) {
    double res = matcher.versionDistance(s2, s1);
    assertEquals(expected1, res, 0.000001);

    res = matcher.versionDistance(s1, s2);
    assertEquals(expected2, res, 0.000001);
  }

}


