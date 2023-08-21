package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.server.AbstractUnitTest;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.roms.ScanResult;
import de.mephisto.vpin.server.util.VPXFileScanner;
import org.junit.jupiter.api.Test;

import java.io.File;

import static de.mephisto.vpin.server.AbstractVPinServerTest.EM_TABLE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TableScanTest extends AbstractUnitTest {

  @Test
  public void testTableScan1() {
    ScanResult scan = VPXFileScanner.scan(new File("../testsystem/vPinball/VisualPinball/Tables/" + EM_TABLE_NAME));
    assertEquals("Baseball_70VPX.txt", scan.getHsFileName());
    assertEquals("Baseball_1970", scan.getRom());
    assertEquals("Baseball_1970", scan.getTableName());
  }

  @Test
  public void testTableScan2() {
    long start = System.currentTimeMillis();
    File table = new File("C:\\vPinball\\VisualPinball\\Tables\\Batman 66.vpx");
    ScanResult scan = VPXFileScanner.scan(table);
    assertEquals("b66_orig", scan.getRom());
  }

  @Test
  public void testTableScan3() throws Exception {
    File table = new File("C:\\vPinball\\VisualPinball\\Tables\\Algar (1980).vpx");
    ScanResult scan = VPXFileScanner.scan(table);
    assertEquals("algar_l1", scan.getRom());
  }

}
