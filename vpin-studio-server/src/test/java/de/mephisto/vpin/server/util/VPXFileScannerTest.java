package de.mephisto.vpin.server.util;

import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.server.roms.ScanResult;
import de.mephisto.vpin.server.vpx.VPXUtil;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class VPXFileScannerTest {

  private final static File scripts = new File("../testsystem/vPinball/VisualPinball/scripts");
  private final static File folder = new File("../testsystem/vPinball/VisualPinball/Tables");
//  private final static File folder = new File("C:\\vPinball\\VisualPinball\\Tables");
//  private final static File folder = new File("C:\\Visual Pinball\\tables");

  @Test
  public void testScan() throws IOException {

    if(folder.exists()) {
      StringBuilder bld = new StringBuilder();
      try (Stream<Path> walkStream = Files.walk(folder.toPath())) {
        walkStream
          .filter(p -> p.getFileName().toString().endsWith("vpx"))
          .forEach(p -> {
            doScan(bld, p);
          });
      }

      try (InputStream in = getClass().getResourceAsStream("scanresult.txt")) {
        String expected = StreamUtils.copyToString(in, Charset.defaultCharset());
        expected = StringUtils.remove(expected, '\r');
        assertEquals(expected, bld.toString());
      }
    }
  }

  private void doScan(StringBuilder bld, Path p) {
    try {
      ScanResult scan = new ScanResult();

      String script = VPXUtil.readScript(p.toFile());
      VPXFileScanner.scanLines(p.toFile(), scripts, scan, script);

      // now log ScanResult
      bld.append(p.getFileName().toFile() + "\n");
      logScan(bld, p, "Rom", scan.getRom());
      logScan(bld, p, "TableName", scan.getTableName());
      logScan(bld, p, "pGameName", scan.getPupPackName());
      String vrroom = scan.isVrRoomSupport() + " / " + scan.isVrRoomDisabled();
      logScan(bld, p, "vrroom", vrroom);
      if (scan.getDMDType() != null) {
        logScan(bld, p, "dmdtype", scan.getDMDType() + ", " + scan.getDMDGameName() + ", " + scan.getDMDProjectFolder());
      }
      bld.append("\n");
    }
    catch (Exception e) {
      fail("Failed to read " + p.toFile().getName());
    }
  }

  private void logScan(StringBuilder bld, Path p, String name, String newWay) {
    bld.append(/*"For " + p.toString() + ", " +*/ name +  " = " + newWay).append("\n");
  }

  @Test
  public void testSingleScan() {
//    File f = new File("C:\\vPinball\\VisualPinball\\Tables\\MF DOOM (GOILL773 2024) v1.1.vpx");
    File f = new File(folder, "Twister (1996).vpx");
    //File f = new File(folder,"Austin Powers (Stern 2001).vpx");
    //File f = new File(folder,"Batman (Data East 1991).vpx");

    if(f.exists()) {
      ScanResult scan = VPXFileScanner.scan(f, scripts);
//      assertTrue(scan.isFoundControllerStop());
      assertNotNull(scan.getRom());
//      assertTrue(scan.isFoundTableExit());

      StringBuilder bld = new StringBuilder();
      doScan(bld, f.toPath());
      System.out.println(bld.toString());
    }
  }

  @Test
  public void testScriptScan() throws IOException {
    File f = File.createTempFile("test", ".vpx");
    String script = "'********************\n" +
        "' Standard definitions\n" +
        "'********************\n" +
        "\n" +
        "Const cGameName = \"avs_170c\" 'Colour Rom\n" +
        "'Const cGameName = \"avs_170\"\n" +
        "\n" +
        "Const UseSolenoids = 1\n" +
        "Const UseLamps = 1\n" +
        "Const UseSync = 1\n" +
        "Const HandleMech = 0";

    ScanResult result = new ScanResult();
    VPXFileScanner.scanLines(f, scripts, result, script);
    FileOutputStream fileOutputStream = new FileOutputStream(f);
    IOUtils.write(script.getBytes(), fileOutputStream);
    fileOutputStream.close();

    assertNotNull(result.getRom());
    assertEquals("avs_170c", result.getRom());
    f.delete();
  }

}


