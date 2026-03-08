package de.mephisto.vpin.server.nvrams;

import de.mephisto.vpin.server.nvrams.parser.NVRamParser;
import de.mephisto.vpin.server.pinemhi.PINemHiService;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class NVRamParserCompareTool {

  public static void main(String[] args) throws IOException, InterruptedException {

    File mainFolder = new File("./testsystem/vPinball/VisualPinball/VPinMAME/nvram/");

    NVRamParser parser = new NVRamParser();
    List<String> supportedNVRams = parser.getSupportedNVRams();
 
    PINemHiService piNemHiService = new PINemHiService();
    String[] split = piNemHiService.getPinemhiSupportedNVRams();
    List<String> pinemhiNVRams = Arrays.asList(split);

    System.out.println("-------------------------------------");
    System.out.println("Missing roms in NVRamMap:");
    for (String s : pinemhiNVRams) {
      if(!supportedNVRams.contains(s)) {
        // load pinhemi and parse scores
        File entry = new File(mainFolder, s + ".nv");
        System.out.println(s + (entry.exists()? " <<< .nv exists" : ""));
      }
    }

    // System.out.println("-------------------------------------");
    // System.out.println("Missing roms in Pinemhi:");
    // for (String s : supportedNVRams) {
    //   if(!pinemhiNVRams.contains(s)) {
    //     System.out.println(s);
    //   }
    // }

    // System.out.println("-------------------------------------");
    // System.out.println("Common roms:");
    // for (String s : pinemhiNVRams) {
    //   if(supportedNVRams.contains(s)) {
    //     System.out.println(s);
    //   }
    // }
  }
 
}
