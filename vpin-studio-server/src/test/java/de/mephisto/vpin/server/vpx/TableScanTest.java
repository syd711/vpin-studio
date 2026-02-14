package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.server.roms.ScanResult;
import de.mephisto.vpin.server.util.VPXFileScanner;
import org.junit.jupiter.api.Test;

import java.io.File;

import static de.mephisto.vpin.server.AbstractVPinServerTest.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TableScanTest {

  private final static File scripts = new File("../testsystem/vPinball/VisualPinball/scripts");
  private final static File folder = new File("../testsystem/vPinball/VisualPinball/Tables");

  @Test
  public void testTableScan1() {
    File table = new File(folder, EM_TABLE_NAME);
    ScanResult scan = VPXFileScanner.scan(table, scripts);
    assertEquals("Baseball_70VPX.txt", scan.getHsFileName());
    assertEquals("Baseball_1970", scan.getRom());
    assertEquals("Baseball_1970", scan.getTableName());
  }

  @Test
  public void testTableScan2() {
    File table = new File(folder, VPREG_TABLE_NAME);
    ScanResult scan = VPXFileScanner.scan(table, scripts);
    assertEquals(VPREG_ROM_NAME, scan.getRom());
  }

  @Test
  public void testTableScan3() throws Exception {
    File table = new File("../testsystem/vPinball/VisualPinball/Tables/" + NVRAM_TABLE_NAME);
    ScanResult scan = VPXFileScanner.scan(table, scripts);
    assertEquals(NVRAM_ROM_NAME, scan.getRom());
  }

//  @Test
//  public void testTableScan4() throws Exception {
//    File table = new File("C:\\vPinball\\VisualPinball\\Tables", "Indianapolis (1995).vpx");
//    ScanResult scan = VPXFileScanner.scan(table);
//    assertEquals("i500_11r", scan.getRom());
//  }

}
