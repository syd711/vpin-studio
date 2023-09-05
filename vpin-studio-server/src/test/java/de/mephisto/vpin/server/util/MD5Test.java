package de.mephisto.vpin.server.util;

import de.mephisto.vpin.server.vpx.VPXUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static de.mephisto.vpin.server.AbstractVPinServerTest.TABLE_NAMES;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class MD5Test {

  @Test
  public void testChecksum() {
    for (String tableName : TABLE_NAMES) {
      File table = new File("../testsystem/vPinball/VisualPinball/Tables/" + tableName);
      String sum = VPXUtil.getChecksum(table);
      System.out.println(sum);
      assertNotNull(sum);
    }
  }
}
