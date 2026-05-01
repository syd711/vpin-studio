package de.mephisto.vpin.server.altsound;

import org.junit.Before;

public class AltSoundTest {
  private AltSoundService altSoundService;

  @Before
  public void setup() {
    altSoundService = new AltSoundService();
  }

//  @Test
//  public void testValidation() {
////    File folder = new File("C:\\vPinball\\VisualPinball\\VPinMAME\\altsound");
//    File folder = new File("../testsystem/vPinball/VisualPinball/VPinMAME/altsound");
//    File[] altSoundFolders = folder.listFiles(pathname -> pathname.isDirectory());
//
//    for (File f : altSoundFolders) {
//      File csvFile = new File(f, "altsound.csv");
//      AltSound altSound = altSoundService.getAltSound(csvFile);
//      assertNotNull(altSound);
//
//      System.out.println("Analyzing " + f.getName());
//      System.out.println("---------------------------------------------------------");
//
//      int count = 0;
//      List<AltSoundEntry> entries = altSound.getEntries();
//      for (AltSoundEntry entry : entries) {
//        if(!entry.isExists()) {
//          count++;
//          System.out.println("Missing File: " + entry.getFilename());
//        }
//      }
//
//      if(count == 0) {
//        System.out.println("No missing files found!");
//      }
//
//      System.out.println("\n\n");
//    }
//  }
}
