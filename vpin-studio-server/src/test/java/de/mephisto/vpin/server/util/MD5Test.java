package de.mephisto.vpin.server.util;

import de.mephisto.vpin.server.vpx.VPXUtil;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


public class MD5Test {

  @Test
  public void testChecksum() {
    File table = new File("../testsystem/vPinball/VisualPinball/Tables/Baseball (1970).vpx");
    String sum1 = VPXUtil.getChecksum(table);

    table = new File("../testsystem/vPinball/VisualPinball/Tables/Baseball2 (1970).vpx");
    String sum2 = VPXUtil.getChecksum(table);

    assertNotEquals(sum1, sum2);
  }
}
