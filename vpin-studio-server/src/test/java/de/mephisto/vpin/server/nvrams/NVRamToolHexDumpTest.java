package de.mephisto.vpin.server.nvrams;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import de.mephisto.vpin.server.nvrams.map.NVRamMap;
import de.mephisto.vpin.server.nvrams.map.SparseMemory;
import de.mephisto.vpin.server.nvrams.tools.NVRamToolHexDump;

/**
 * Take a rom and dump its hex characters, not really a test...
 */
public class NVRamToolHexDumpTest {

  @Test
  public void testDumpAlpok() throws IOException {
    doTestDump("alpok_l6");
  }

  @Test
  public void testDumpAfm() throws IOException {
    doTestDump("afm_113b");
  }

  private void doTestDump(String rom) throws IOException {
    File mainFolder = new File("../testsystem/vPinball/VisualPinball/VPinMAME/nvram");

    File entry = new File(mainFolder, rom + ".nv");

    byte[] bytes = Files.readAllBytes(entry.toPath());

    NVRamMapService parser = new NVRamMapService();
    NVRamMap mapJson = new NVRamMap();
    SparseMemory memory = parser.getMemory(mapJson, bytes);
    NVRamToolHexDump dump = new NVRamToolHexDump();
    String txt = dump.hexDump(mapJson, memory, Locale.ENGLISH);

    System.out.println(txt);
  }
}
