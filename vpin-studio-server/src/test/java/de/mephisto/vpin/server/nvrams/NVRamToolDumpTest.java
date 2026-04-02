package de.mephisto.vpin.server.nvrams;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.mephisto.vpin.server.nvrams.map.NVRamMap;
import de.mephisto.vpin.server.nvrams.map.SparseMemory;
import de.mephisto.vpin.server.nvrams.tools.NVRamToolDump;

/**
 */
public class NVRamToolDumpTest {

  @Test
  public void testDumpAlpok() throws IOException {
    doTestDump("alpok_l6");
  }

  /** @fixme check how to limit nb of scores as buy-in scores are added at the moment */
  //@Test
  public void testDumpAfm() throws IOException {
    doTestDump("afm_113b");
  }


  private void doTestDump(String rom) throws IOException {
    File mainFolder = new File("../testsystem/vPinball/VisualPinball/VPinMAME/nvram");
    
    File entry = new File(mainFolder, rom + ".nv");
    byte[] bytes = Files.readAllBytes(entry.toPath());

    File entryList = new File(mainFolder, rom + ".nv.list");
    String expected = Files.readString(entryList.toPath());

    NVRamMapService parser = new NVRamMapService();
    NVRamMap mapJson = parser.getMap(rom);
    SparseMemory memory = parser.getMemory(mapJson, bytes);

    NVRamToolDump dump = new NVRamToolDump();
    //String txt = dump.dump(mapJson, memory, Locale.ENGLISH, true);

    String rawscores = dump.dumpScores(mapJson, memory, Locale.GERMANY, false);

    Assertions.assertEquals(expected, rawscores);
  }
}
