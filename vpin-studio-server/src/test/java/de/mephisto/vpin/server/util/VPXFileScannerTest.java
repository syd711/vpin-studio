package de.mephisto.vpin.server.util;

import de.mephisto.vpin.server.roms.ScanResult;
import de.mephisto.vpin.server.vpx.VPXUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

import static org.junit.jupiter.api.Assertions.*;


public class VPXFileScannerTest {

//  private final static File folder = new File("../testsystem/vPinball/VisualPinball/Tables");
  private final static File folder = new File("C:\\vPinball\\VisualPinball\\Tables");

  @Test
  public void testScan() {
    if(folder.exists()) {
      File[] vpxfiles = folder.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith("vpx");
        }
      });

      System.out.println("Scanning " + vpxfiles.length + " vpx files.");
      for (File vpxfile : vpxfiles) {
        ScanResult scan = VPXFileScanner.scan(vpxfile);
        if (scan.isVrRoomDisabled()) {
          System.out.println(vpxfile.getAbsolutePath());
        }
      }
    }
  }

  @Test
  public void testSingleScan() {
    File folder = new File("../testsystem/vPinball/VisualPinball/Tables");
//    File f = new File("C:\\vPinball\\VisualPinball\\Tables\\MF DOOM (GOILL773 2024) v1.1.vpx");
    File f = new File(folder,"Twister (1996).vpx");
    if(f.exists()) {
      ScanResult scan = VPXFileScanner.scan(f);
//      assertTrue(scan.isFoundControllerStop());
      assertNotNull(scan.getRom());
//      assertTrue(scan.isFoundTableExit());
    }
  }
}


