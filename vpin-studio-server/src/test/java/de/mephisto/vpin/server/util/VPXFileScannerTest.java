package de.mephisto.vpin.server.util;

import de.mephisto.vpin.server.roms.ScanResult;
import de.mephisto.vpin.server.scripteval.EvaluationContext;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
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

            compare(bld, p, scan);
          });
        fw.append(bld);
        fw.flush();
      }
    }

    }
  }

  private void compare(StringBuilder bld, Path p, ScanResult scan) {

    //FIXME REMOVE FOR PROD, JUST HERE TO COMPARE RESULT FROM NEW SCAN WITH OLD ONE
    EvaluationContext evalctxt = scan.evalctxt;

    if (scan.getGameName() != null) {
      diff(bld, p, "Rom", scan.getRom(), scan.getGameName());
    }

    diff(bld, p, "TableName", scan.getTableName(), evalctxt.getVarValue("B2STableName"));
    diff(bld, p, "pGameName", scan.getPupPackName(), evalctxt.getVarValue("pGameName"));

    Object vrroom = ObjectUtils.firstNonNull(evalctxt.getVarValue("vrroom"), evalctxt.getVarValue("vr_room"));
    if (vrroom != null ||  scan.isVrRoomSupport()) {
      String oldvrromm = scan.isVrRoomSupport() + " / " + scan.isVrRoomDisabled();
      String newvrroom = (vrroom != null) + " / " + (vrroom != null && vrroom.toString().equals("0"));
      diff(bld, p, "vrroom", oldvrromm, newvrroom);
    }

    if (scan.getDMDType() != null) {
    //  bld.append("For " + p.toString() + ", " + scan.getDMDType() + ", " + scan.getDMDGameName() + ", " + scan.getDMDProjectFolder());
    //  bld.append("\n");
    }
  }

  private void diff(StringBuilder bld, Path p, String name, String oldWay, String newWay) {
    if (!StringUtils.equals(oldWay, newWay)) {
      bld.append("For " + p.toString() + ", " + name +  " = " + newWay + " vs/old " + oldWay);
      bld.append("\n");
    }
  }


  @Test
  public void testSingleScan() {
//    File f = new File("C:\\vPinball\\VisualPinball\\Tables\\MF DOOM (GOILL773 2024) v1.1.vpx");
    //File f = new File(folder,"Twister (1996).vpx");
    File f = new File(folder,"007 Goldeneye (Sega 1996).vpx");

    if(f.exists()) {
      ScanResult scan = VPXFileScanner.scan(f);
//      assertTrue(scan.isFoundControllerStop());
      assertNotNull(scan.getRom());
//      assertTrue(scan.isFoundTableExit());

      StringBuilder bld = new StringBuilder();
      compare(bld, f.toPath(), scan);
      System.out.println(bld.toString());
    }
  }

}


