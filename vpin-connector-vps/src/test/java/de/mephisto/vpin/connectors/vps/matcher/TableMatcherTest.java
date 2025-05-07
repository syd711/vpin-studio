package de.mephisto.vpin.connectors.vps.matcher;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class TableMatcherTest {
  private final static Logger LOG = LoggerFactory.getLogger(TableMatcherTest.class);

  @Test
  public void testDistance() {
    TableMatcher matcher = new TableMatcher(null);

    doTest(matcher, "Cactus Canyon", "cactus canyon", 0);
    doTest(matcher, "CactusCanyon", "Cactus Canyon", 0.15);
    doTest(matcher, "CactusCanyon", "the Cactus  Canyon", 0.3);
    doTest(matcher, "Cactus Canion", "Cactus Canyon", 0.15);
    doTest(matcher, "Cactus Canyon", "cactus canyon continued", 0.45);
    doTest(matcher, "Cactus Canyon continued", "Cactus Canyon Cont.", 0.15);

    doTest(matcher, "cactus canyon", "Cactus Canyon", "cactus canyon continued");
    doTest(matcher, "cactus canion", "Cactus Canyon", "cactus canyon continued");
    doTest(matcher, "cactus canyon continued", "cactus canyon continued", "Cactus Canyon");

    doTest(matcher, "getaway, the - high speed ii v1.1", "The Getaway - High Speed II", 0.15);
    doTest(matcher, "getaway, the - high speed ii v1.1", "The Getaway - High Speed II", "v.1");

    //-------------
    doTest(matcher, "Pet Semetary", "Stephen King's Pet Semetary", 0.15);
    doTest(matcher, "Stephen King's Pet Semetary", "Pet Sematary of Stephen King", "Met Cimantery");

    //-------------
    doTest(matcher, "cavalcade", "ava", 0.5);
    doTest(matcher, "cavalcade", "aval", 0.3);
    doTest(matcher, "cavalcade", "avalc", 0.25);
    doTest(matcher, "cavalcade", "avat", 0.75);
    doTest(matcher, "avalc", "cavalcade", "cavalcade plus long");
  }

  /**
   * Check s1 is closed to s2 (distance < 2))
   */
  private void doTest(TableMatcher matcher, String s1, String s2, double maxDistance) {
    double d = matcher.distance(s1, s2);
    assertTrue(d <= maxDistance);

    d = matcher.distance(s2, s1);
    assertTrue(d <= maxDistance);
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

  @Test
  public void testAllClosest() {
    // load the database from resources
    VPS vpsDatabase = new VPS();
    vpsDatabase.reload();

    VpsDebug debug = new VpsDebug();
    TableMatcher matcher = new TableMatcher(debug);

    doMatch(matcher, vpsDatabase, 1, "hmDnWj5wzP", "Attack on Titan (Original 2022)", "Attack on Titan", "original", 2022, debug);
    doMatch(matcher, vpsDatabase, 1, "vZDUDUii", "Cactus Canion (Bally 1998)", "Cactus Canion", "Bally", 1998, debug);
    doMatch(matcher, vpsDatabase, 2, "fYRdzeuYYN", "Bad (Original 2022)", "Bad", "Original", 2021, debug);
  }

  private void doMatch(TableMatcher matcher, VPS vpsDatabase, int nbExpectedTables, String tableId, String fileName, String tableName, String manuf, int year, VpsDebug debug) {
    List<VpsTable> tables = matcher.findAllClosest(fileName, null, tableName, manuf, year, vpsDatabase.getTables());
    LOG.error(debug.toString());
    assertEquals(nbExpectedTables, tables.size());
    // ensure first table returned is the expected one
    assertEquals(tableId, tables.get(0).getId());

    debug.clear();
    // do same test but without the parsed parts
    tables = matcher.findAllClosest(fileName, null, null, null, -1, vpsDatabase.getTables());
    LOG.error(debug.toString());
    assertEquals(nbExpectedTables, tables.size());
    assertEquals(tableId, tables.get(0).getId());
  }

}
