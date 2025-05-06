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

  @Test
  public void testAllClosest() {
    // load the database from resources
    VPS vpsDatabase = new VPS();
    vpsDatabase.reload();

    VpsDebug debug = new VpsDebug();
    TableMatcher matcher = new TableMatcher(debug);

    doMatch(matcher, vpsDatabase, 1, "hmDnWj5wzP", "Attack on Titan (Original 2022)", "Attack on Titan", "original", 2022, debug);
    doMatch(matcher, vpsDatabase, 1, "vZDUDUii", "Cactus Canion (Bally 1998)", "Cactus Canion", "Bally", 1998, debug);
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
