package de.mephisto.vpin.server.nvrams;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

import de.mephisto.vpin.server.nvrams.parser.NVRamMap;
import de.mephisto.vpin.server.nvrams.parser.NVRamParser;
import de.mephisto.vpin.server.nvrams.parser.NVRamToolDump;
import de.mephisto.vpin.server.nvrams.parser.SparseMemory;

/**
 */
public class NVRamParserTool {

  public static void main(String[] args) {
    NVRamParserTool tool = new NVRamParserTool();
    tool.run("alpok_l6", "maps/gottlieb/system80b/80b-7digit-8KB.map.json");
  }

  public void run(String rom, String mapPath) {
    try  {
      //File mainFolder = new File("./testsystem/vPinball/VisualPinball/VPinMAME/nvram");
      File mainFolder = new File("C:/Visual Pinball/VPinMAME/nvram");

      File entry = new File(mainFolder, rom + ".nv");
      byte[] bytes = Files.readAllBytes(entry.toPath());

      NVRamParser parser = new NVRamParser();
      NVRamMap mapJson = parser.getMapFromPath(mapPath);
      SparseMemory memory = parser.setNvram(mapJson, bytes);

      NVRamToolDump dump = new NVRamToolDump();
      String txt = dump.dump(mapJson, memory, Locale.ENGLISH, true);

      System.out.println(txt);
    }
    catch (IOException ioe) {
    }
  }
}
