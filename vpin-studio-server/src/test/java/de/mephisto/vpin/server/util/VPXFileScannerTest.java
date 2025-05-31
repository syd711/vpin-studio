package de.mephisto.vpin.server.util;

import de.mephisto.vpin.server.roms.ScanResult;

import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class VPXFileScannerTest {

  //private final static File folder = new File("../testsystem/vPinball/VisualPinball/Tables");
  //private final static File folder = new File("C:\\vPinball\\VisualPinball\\Tables");
  private final static File folder = new File("C:\\Visual Pinball\\tables");

  @Test
  public void testScan() throws IOException {

    try (FileWriter fw = new FileWriter("c:/temp/diffs.txt")) {

    if(folder.exists()) {
      try (Stream<Path> walkStream = Files.walk(folder.toPath())) {
        StringBuilder bld = new StringBuilder();
        walkStream
          .filter(p -> p.getFileName().toString().endsWith("vpx"))
          .forEach(p -> {
            ScanResult scan = VPXFileScanner.scan(p.toFile());
            if (!StringUtils.equals(scan.getRom(), scan.getGameName())) {
              bld.append("For " + p.toString() + ", rom = " + scan.getRom() + ", gameName = " + scan.getGameName());
            }
            if (scan.getDMDType() != null) {
              bld.append("For " + p.toString() + ", " + scan.getDMDType() + ", " + scan.getDMDGameName() + ", " + scan.getDMDProjectFolder());
              bld.append("\n");
            }
          });
        fw.append(bld);
        fw.flush();
      }
    }

    }
  }

  @Test
  public void testSingleScan() {
//    File f = new File("C:\\vPinball\\VisualPinball\\Tables\\MF DOOM (GOILL773 2024) v1.1.vpx");
    //File f = new File(folder,"Twister (1996).vpx");
    File f = new File(folder,"Agents 777 (Game Plan 1984) hsm.vpx");

    if(f.exists()) {
      ScanResult scan = VPXFileScanner.scan(f);
//      assertTrue(scan.isFoundControllerStop());
      assertNotNull(scan.getRom());
//      assertTrue(scan.isFoundTableExit());

      if (!StringUtils.equals(scan.getRom(), scan.getGameName())) {
        System.out.println("For " + f.toString() + ", rom = " + scan.getRom() + ", gameName = " + scan.getGameName());
      }
    }
  }

}


