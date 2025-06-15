package de.mephisto.vpin.server.util;

import de.mephisto.vpin.server.roms.ScanResult;
import de.mephisto.vpin.server.scripteval.EvaluationContext;
import de.mephisto.vpin.server.vpx.VPXUtil;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
            compare(bld, p);
          });
        fw.append(bld);
        fw.flush();
      }
    }

    }
  }

  //FIXME REMOVE FOR PROD, JUST HERE TO COMPARE RESULT FROM NEW SCAN WITH OLD ONE
  private void compare(StringBuilder bld, Path p) {

    ScanResult scan = new ScanResult();

    String script = VPXUtil.readScript(p.toFile());

    List<String> allLines = new ArrayList<>();
    script = script.replaceAll("\r\n", "\n");
    script = script.replaceAll("\r", "\n");
    allLines.addAll(Arrays.asList(script.split("\n")));
    Collections.reverse(allLines);

    EvaluationContext evalctxt = new EvaluationContext();

    VPXFileScanner.scanLines(p.toFile(), scan, evalctxt, allLines);

    if (scan.getGameName() != null) {
      diff(bld, p, "Rom", scan.getRom(), scan.getGameName());
    }

    String tableName = StringUtils.defaultString(evalctxt.getVarValue("TableName"), evalctxt.getVarValue("B2STableName"));
    diff(bld, p, "TableName", scan.getTableName(), tableName);
    diff(bld, p, "pGameName", scan.getPupPackName(), evalctxt.getVarValue("pGameName"));

    Object vrroom = ObjectUtils.firstNonNull(evalctxt.getVarValue("vrroom"), evalctxt.getVarValue("vr_room"));
    if (vrroom != null ||  scan.isVrRoomSupport()) {
      String oldvrromm = scan.isVrRoomSupport() + " / " + scan.isVrRoomDisabled();
      String newvrroom = (vrroom != null) + " / " + (vrroom == null || vrroom.toString().equals("0"));
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
    //File f = new File(folder,"Austin Powers (Stern 2001).vpx");
    File f = new File(folder,"Batman (Data East 1991).vpx");

    if(f.exists()) {
      ScanResult scan = VPXFileScanner.scan(f);
//      assertTrue(scan.isFoundControllerStop());
      assertNotNull(scan.getRom());
//      assertTrue(scan.isFoundTableExit());

      StringBuilder bld = new StringBuilder();
      compare(bld, f.toPath());
      System.out.println(bld.toString());
    }
  }

}


