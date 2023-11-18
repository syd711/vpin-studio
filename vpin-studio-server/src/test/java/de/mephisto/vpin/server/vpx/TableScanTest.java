package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.server.roms.ScanResult;
import de.mephisto.vpin.server.util.VPXFileScanner;
import org.junit.jupiter.api.Test;

import java.io.File;

import static de.mephisto.vpin.server.AbstractVPinServerTest.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TableScanTest {

  @Test
  public void testTableScan1() {
    ScanResult scan = VPXFileScanner.scan(new File("../testsystem/vPinball/VisualPinball/Tables/" + EM_TABLE_NAME));
    assertEquals("Baseball_70VPX.txt", scan.getHsFileName());
    assertEquals("Baseball_1970", scan.getRom());
    assertEquals("Baseball_1970", scan.getTableName());
  }

  @Test
  public void testTableScan2() {
    File table = new File("../testsystem/vPinball/VisualPinball/Tables/" + VPREG_TABLE_NAME);
    ScanResult scan = VPXFileScanner.scan(table);
    assertEquals(VPREG_ROM_NAME, scan.getRom());
  }

  @Test
  public void testTableScan3() throws Exception {
    File table = new File("../testsystem/vPinball/VisualPinball/Tables/" + NVRAM_TABLE_NAME);
    ScanResult scan = VPXFileScanner.scan(table);
    assertEquals(NVRAM_ROM_NAME, scan.getRom());
  }

//  @Test
//  public void testTableScan4() throws Exception {
//    File table = new File("C:\\vPinball\\VisualPinball\\Tables", "007.Goldeneye - MOD Version 1.0.vpx");
//    ScanResult scan = VPXFileScanner.scan(table);
//    assertEquals("gldneye", scan.getRom());
//  }

}
