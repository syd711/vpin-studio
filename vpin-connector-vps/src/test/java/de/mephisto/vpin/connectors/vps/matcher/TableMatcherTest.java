package de.mephisto.vpin.connectors.vps.matcher;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TableMatcherTest {

  @Test
  public void testDistance() {
    TableMatcher matcher = new TableMatcher();

    doTest(matcher, "Cactus Canyon", "cactus canyon");
    doTest(matcher, "CactusCanyon", "Cactus Canyon");
    doTest(matcher, "CactusCanyon", "the Cactus  Canyon");
    doTest(matcher, "Cactus Canion", "Cactus Canyon");

    doTest(matcher, "Cactus Canyon continued", "Cactus Canyon Cont.");

    doTest(matcher, "cactus canyon", "Cactus Canion", "cactus canyon continued");

    doTest(matcher, "getaway, the - high speed ii v1.1", "The Getaway - High Speed II");
    doTest(matcher, "getaway, the - high speed ii v1.1", "The Getaway - High Speed II", "v.1");




    //-------------
    doTest(matcher, "Pet Semetary", "Stephen King's Pet Semetary");
    doTest(matcher, "Stephen King's Pet Semetary", "Pet Sematary of Stephen King", "Met Cimantery");

  }

  /**
   * Check s1 is closed to s2 (distance < 2))
   */
  private void doTest(TableMatcher matcher, String s1, String s2) {
    double d = matcher.distance(s1, s2);
    assertTrue(d < 2);

    d = matcher.distance(s2, s1);
    assertTrue(d < 2);
  }

  /**
   * Check s1 is closer from s2 than s3
   */
  private void doTest(TableMatcher matcher, String s1, String s2, String s3) {
    double d12 = matcher.distance(s1, s2);
    double d13 = matcher.distance(s1, s3);
    assertTrue(d12 < d13);

    double d21 = matcher.distance(s2, s1);
    double d31 = matcher.distance(s3, s1);
    assertTrue(d21 < d31);
  }
}
