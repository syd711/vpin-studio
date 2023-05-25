package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.server.roms.ScanResult;
import de.mephisto.vpin.server.util.VPXFileScanner;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TableScanTest {

  @Test
  public void testTableScan1() {
    File table = new File("C:\\vPinball\\VisualPinball\\Tables\\Hayburners (WIlliams 1951).vpx");
    ScanResult scan = VPXFileScanner.scan(table);
    assertEquals("Hayburners_51VPX.txt", scan.getHsFileName());
    assertEquals("Hayburners_1951", scan.getRom());
    assertEquals("Hayburners_1951", scan.getTableName());
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
