package de.mephisto.vpin.server.score;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

public class DMDScoreTest {

  private String gameName = "test";

  @Test
  public void testScanImage() throws Exception {
    Frame f = parseFrame("_waiting.frame");
    String txt = doProcess(new DMDScoreScannerTessAPI(false), f, "testScanImage");
    assertEquals("WAITING FOR\nSWITCH SCANNING", txt);
  }

  @Test
  public void testBlankIndex() {
    DMDScoreGameReplayer replayer = new DMDScoreGameReplayer();
    DMDScoreProcessorBase p = new DMDScoreProcessorImageDump();
    assertEquals(0, p.getBlankIndex(replayer.parsePalette("[0,16711680,0,0]")));
    assertEquals(1, p.getBlankIndex(replayer.parsePalette("[4868863,0,7829367,0]")));
    assertEquals(1, p.getBlankIndex(replayer.parsePalette("[16767492,0,16711680,151]")));
    assertEquals(2, p.getBlankIndex(replayer.parsePalette("[16760962,8405056,0,11310948]")));
    assertEquals(0, p.getBlankIndex(replayer.parsePalette("[20736,16777215,65280,0]")));
    assertEquals(0, p.getBlankIndex(replayer.parsePalette("[20736,16777215,65280,34589]")));
    //assertEquals(1, p.getBlankIndex(parsePalette("[16760962,0,0,0]")));
  }

  //----------------------

  @Test
  public void testSplitAndScan() throws Exception {
    doTestFrame("_2sides_1.frame", 
            "797,020\n" +
            "650,700\n" +
            //------------------------
            "HIGH SCORE #1\n" + 
            "LR\n" +             
            "55,000,000"
    );
  }

  @Test
  public void testSplitAndScan2() throws Exception {
    doTestFrame("_2sides_2.frame", 
            "797,020\n" +
            "650,700\n" +
            //------------------------
            "FINAL BATTLE CHAMPION\n" + 
            "LFS\n" +
            "25,000,000"
    );
  }

  @Test
  public void testSplitAndScanInvertedPalette() throws Exception {
    doTestFrame("_2sides_invertedpalette.frame", 
            "797,020\n" +
            "650,700\n" +
            //------------------------
            "FINAL BATTLE CHAMPION\n" + 
            "LFS\n" +
            "25,000,000"
    );
  }

  

  @Test
  public void testFrame0() throws Exception {
    doTestFrame("_frame0.frame",  
            "00\n" +
            "00\n" +
            "BALL 1 CREDITS 0"
    );
  }

  @Test
  public void testFrameWithImage() throws Exception {
    doTestFrame("_withimage_1.frame",
            "00\n" +
            "00\n" +
            "00 TI)\n" +    // TI) is small blue <1> circle detected !
            "BALL 1 CREDITS 0"
    );
  }

  @Test
  public void testFrameWithImage2() throws Exception {
    // some artefacts on frame after removal of images prevent a good recognition
    doTestFrame("_withimage_2.frame",
            "168020,\n" +
            "" +  // should be "00\n" +
            "00\n" +
            "BALL 1 CREDITS,O"  // should be "BALL 1 CREDITS 0\n"
    );
  }

  @Test
  public void testFrameWithShadowText() throws Exception {
    // from Creature from Black Lagoon
    doTestFrame("_withshadow.frame",
            "1500.000\n" + 
            "BALL 1 CREDITS 0"
    );
  }

  @Test
  public void testFrameWithBorder() throws Exception {
    doTestFrame("_border_1.frame", 
            "BALL 1\nCREDITS 0"
    );
  }

  @Test
  public void testFrameWithBorderInvertedPalette() throws Exception {
    doTestFrame("_border_2.frame",
            "BALL 1\nCREDITS 0"
    );
  }

  @Test
  public void testFrameWithVSplitAndBorder() throws Exception {
    doTestFrame("_border_3.frame", 
            "417,650\n" +
            "TOTAL BONUS\n" +
            "135,050"
    );
  }

  @Test
  public void testFrameTwoFonts1() throws Exception {
    doTestFrame("_twofonts_1.frame",
            "1,811,100\n" +
            "3,529,060\n" +
            "BALL 3 CREDITS 0"
    );
  }

  @Test
  public void testFrameTwoFonts2() throws Exception {
    doTestFrame("_twofonts_2.frame",
            "190,040\n" +
            "15,000\n" +
            "BALL 1 CREDITS 1V2"
    );
  }

  @Test
  public void testFrameTwoFonts3() throws Exception {
    doTestFrame("_twofonts_3.frame",
            "11,348,130 12,934,180\n" + 
            "CREDITS 0"
    );
  }

  @Test
  public void testFrameWithColoredBackground() throws Exception {
    // from Creature from Black Lagoon
    doTestFrame("_withbackgound.frame",
            "1500.000\n" + 
            "BALL 1 CREDITS 0"
    );
  }

  @Test
  public void testFrameWithSpace3BetweenCharacters() throws Exception {
    // from Cactus Canyon
    doTestFrame("_bigspace_1.frame",
            "1.114.020\n" +
            "BALL 2 CREDITS 1"
    );
  }

  @Test
  public void testFrameWithSpace7BetweenCharacters() throws Exception {
    // from Cactus Canyon
    doTestFrame("_bigspace_2.frame",
            "195,800\n" +
            "3,170\n" +
            "(95800\n" + // should be 195800 but too strange font taken from game '24'
            "REPLAY AT 20,000,000"
    );
  }
  
  @Test
  public void testFrameFontWithSegments() throws Exception {
    // from Ace of Speed (mousn_l4)
    String frameFile = "_fontwithsegments.frame";
    Frame f = parseFrame(frameFile);
    DMDScoreScannerLCD proc = new DMDScoreScannerLCD(DMDScoreScannerLCD.Type.WIDTH_7);
    String txt = doProcess(proc, f, StringUtils.substringBefore(frameFile, ".")); 
    assertEquals("10000\n" + 
       "BALL 1", txt);
  }

  @Test
  public void testFrameFontWithSmallSegments() throws Exception {
    // from 250 cc (Inder) (ind250cc)
    String frameFile = "_fontwithsegments_small.frame";
    Frame f = parseFrame(frameFile);
    DMDScoreScannerLCD proc = new DMDScoreScannerLCD(DMDScoreScannerLCD.Type.WIDTH_5);
    String txt = doProcess(proc, f, StringUtils.substringBefore(frameFile, ".")); 
    assertEquals("043301\n" + 
       "0103", txt);
  }

  


  //----------------------

  protected void doTestFrame(String frameFile, String expected) throws Exception {
    Frame f = parseFrame(frameFile);
    DMDScoreSplitAndScan proc = new DMDScoreSplitAndScan();
    String txt = doProcess(proc, f, StringUtils.substringBefore(frameFile, ".")); 
    assertEquals(expected, txt);
  }
  
  private String doProcess(DMDScoreProcessor processor, Frame frame, String testname) {
    // wrap proc to also generate the imae :
    DMDScoreProcessorDelegate delegate = new DMDScoreProcessorDelegate(new DMDScoreProcessorImageDump(), processor);

    try {
      DMDScoreScannerTessAPI.TESSERACT_FOLDER = "." + DMDScoreScannerTessAPI.TESSERACT_FOLDER;

      delegate.onFrameStart(gameName);
      long timeStart = System.currentTimeMillis();
      List<FrameText> texts = new ArrayList<>();
      delegate.onFrameReceived(frame, texts);
      long time = System.currentTimeMillis() - timeStart;
      System.out.println("test " + testname + " took " + time);

      StringBuilder bld = new StringBuilder();
      for (FrameText text : texts) {
        if (bld.length() > 0) {
          bld.append("\n");
        }
        bld.append(text.getText());
      }
      return bld.toString();
    }
    finally {
      delegate.onFrameStop(gameName);
      DMDScoreScannerTessAPI.TESSERACT_FOLDER = DMDScoreScannerTessAPI.TESSERACT_FOLDER.substring(1);
    }
  }

  public Frame parseFrame(String frameName) throws Exception{
    DMDScoreGameReplayer replayer = new DMDScoreGameReplayer();
    try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(
            getClass().getResourceAsStream(frameName), Charset.forName("UTF-8")))) {
      return replayer.readOneFrame(reader);
    }
  }
}