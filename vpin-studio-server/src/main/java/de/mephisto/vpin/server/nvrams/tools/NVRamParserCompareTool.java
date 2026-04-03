package de.mephisto.vpin.server.nvrams.tools;

import de.mephisto.vpin.server.nvrams.NVRamMapService;
import de.mephisto.vpin.server.pinemhi.PINemHiService;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * A Tool that compares NVRAm support between pinemHi and the Nvram-Map project
 */
public class NVRamParserCompareTool {

  public static void main(String[] args) throws IOException, InterruptedException {

    NVRamMapService parser = new NVRamMapService();
    List<String> supportedNVRams = parser.getSupportedNVRams();

    File[] testFolders = new File[] { 
      new File("./testsystem/vPinball/VisualPinball/VPinMAME/nvram/"),
      new File("C:/Github/py-pinmame-nvmaps/test/nvram"),
      new File("C:/temp/_NVRAMS/Matt"),
      new File("C:/temp/_NVRAMS/ed209"),
      new File("C:/temp/_NVRAMS/YabbaDabbaDoo"),
      new File("C:/temp/_NVRAMS/gonzonia"),
      new File("C:/temp/_NVRAMS/GerhardK"),
      new File("C:/temp/_NVRAMS/Buffdriver"),
      new File("C:/temp/_NVRAMS/BostonBuckeye"),
      new File("C:/temp/_NVRAMS/FuFu"),
      new File("C:/temp/_NVRAMS/Blap"),
      new File("C:/Visual Pinball/VPinMAME/nvram"),         // OLE
      new File("./resources/nvrams")    // resetted nvrams
    };

    String[] split = PINemHiService.getPinemhiSupportedNVRams();
    List<String> pinemhiNVRams = Arrays.asList(split);

    try (PrintWriter w = new PrintWriter("nvam_vs_pinemhi.txt")) {
      w.println("-------------------------------------");
      w.println("Missing roms in NVRamMap vs in Pinemhi:");
      for (String s : pinemhiNVRams) {
        if(!supportedNVRams.contains(s)) {
          // load pinhemi and parse scores
          String paths = null;
          for (File folder : testFolders) {
            File entry = new File(folder, s + ".nv");
            if (entry.exists()) {
              paths = (paths != null? paths + ", ": "") + entry.getAbsolutePath();
            }
          }
          w.println(s + (paths != null ? " <<< .nv exists : " + paths : ""));
        }
        else {
          w.println(s + " OK");
        }
      }

      // w.println("-------------------------------------");
      // w.println("Missing roms in Pinemhi:");
      // for (String s : supportedNVRams) {
      //   if(!pinemhiNVRams.contains(s)) {
      //     w.println(s);
      //   }
      // }

      // w.println("-------------------------------------");
      // w.println("Common roms:");
      // for (String s : pinemhiNVRams) {
      //   if(supportedNVRams.contains(s)) {
      //     w.println(s);
      //   }
      // }
    }
  }
}
