package de.mephisto.vpin.server.nvrams.tools;

import de.mephisto.vpin.server.nvrams.NVRamMapService;
import de.mephisto.vpin.server.nvrams.NVRamMapSuperhacService;
import de.mephisto.vpin.server.pinemhi.PINemHiService;
import de.mephisto.vpin.server.vpinmame.VPinMameService;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A Tool that compares NVRAm support between pinemHi and the Nvram-Map project
 */
public class NVRamParserCompareTool {

  public static void main(String[] args) throws IOException {

    VPinMameService vpinmameService = new VPinMameService();
    Map<String, String> roms = vpinmameService.getRomnames(new File("C:/Visual Pinball/VPinMAME"));
    Map<String, String> clones = vpinmameService.getClones(new File("C:/Visual Pinball/VPinMAME"));

    NVRamMapService parser = new NVRamMapService();
    List<String> supportedNVRams = parser.getSupportedNVRams();

    String[] split = PINemHiService.getPinemhiSupportedNVRams();
    List<String> pinemhiNVRams = Arrays.asList(split);
    
    NVRamMapSuperhacService superhac = new NVRamMapSuperhacService();
    List<String> supportedbySuperhac = superhac.getSupportedNVRams();

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

    try (PrintWriter w = new PrintWriter("allroms.csv")) {
      w.println("\"rom\",\"Name\",\"clone of\",\"pinemHi\",\"tomslogic\",\"superhac\",\"nvs\"");
      w.println("-------------------------------------");
      for (String s : roms.keySet()) {

        w.print(s + ",");
        w.print(roms.get(s) + ",");

        String cloneOf = clones.get(s);
        w.print((cloneOf != null? cloneOf: "") + ",");

        w.print((pinemhiNVRams.contains(s) ? "x": "") + ",");
        w.print((supportedNVRams.contains(s) ? "x": "") + ",");
        w.print((supportedbySuperhac.contains(s) ? "x": "") + ",");

        String paths = null;
        for (File folder : testFolders) {
          File entry = new File(folder, s + ".nv");
          if (entry.exists()) {
            paths = (paths != null? paths + ", ": "") + entry.getAbsolutePath();
          }
        }
        w.println(paths != null ? paths: "");
      }
    }
  }
}
