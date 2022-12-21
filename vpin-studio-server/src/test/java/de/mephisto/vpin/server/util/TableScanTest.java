package de.mephisto.vpin.server.util;

import de.mephisto.vpin.server.roms.ScanResult;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TableScanTest {

  @Test
  public void testTableScan1()  {
    File table = new File("C:\\vPinball\\VisualPinball\\Tables\\Hayburners (WIlliams 1951).vpx");
    ScanResult scan = VPXFileScanner.scan(table);
    assertEquals("Hayburners_51VPX.txt", scan.getHsFileName());
    assertEquals("Hayburners_1951", scan.getRom());
    assertEquals("Hayburners_1951", scan.getTableName());
  }

  @Test
  public void testTableScan2() throws Exception {
    File table = new File("C:\\vPinball\\VisualPinball\\Tables\\Guns N Roses.vpx");
    ScanResult scan = VPXFileScanner.scan(table);
    assertEquals("gnr_300", scan.getRom());
  }

  @Test
  public void testTableScan3() throws Exception {
    File table = new File("C:\\vPinball\\VisualPinball\\Tables\\Masters of the Universe.vpx");
    ScanResult scan = VPXFileScanner.scan(table);
    assertEquals("motu", scan.getRom());
  }

}
