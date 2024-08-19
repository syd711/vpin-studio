package de.mephisto.vpin.server.util;

import de.mephisto.vpin.server.roms.ScanResult;
import de.mephisto.vpin.server.vpx.VPXUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class VPXFileScannerTest {

//  @Test
//  public void testScan() {
//    File files = new File("C:\\vPinball\\VisualPinball\\Tables");
//    File[] vpxfiles = files.listFiles(new FilenameFilter() {
//      @Override
//      public boolean accept(File dir, String name) {
//        return name.endsWith("vpx");
//      }
//    });
//
//    System.out.println("Scanning " + vpxfiles.length + " vpx files.");
//    for (File vpxfile : vpxfiles) {
//      ScanResult scan = VPXFileScanner.scan(vpxfile);
//      if (scan.getRom() != null && !scan.isFoundControllerStop() && scan.isFoundTableExit()) {
//        System.err.println(vpxfile.getAbsolutePath());
//      }
//    }
//  }
//
//  @Test
//  public void testSingleScan() {
//    ScanResult scan = VPXFileScanner.scan(new File("C:\\vPinball\\VisualPinball\\Tables\\Funhouse (1990).vpx"));
//    assertTrue(scan.isFoundControllerStop());
//    assertTrue(scan.isFoundTableExit());
//  }
}


