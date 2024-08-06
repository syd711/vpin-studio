package de.mephisto.vpin.server.score;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import com.google.common.io.Files;

public class DMDScoreTest {

  private String gameName = "test";
  private int w = 128;
  private int h = 32;

  @Test
  public void testSaveFrame() throws Exception {
    Frame f = parseFrame("_waiting.frame");
    String out = doProcess(new DMDScoreProcessorFrameDump(), f, "testSaveFrame");

    File file = new File(getClass().getResource("_waiting.frame").toURI());
    String expected = FileUtils.readFileToString(file, "UTF-8");
    expected = expected.replace(System.lineSeparator(), "\n");

    assertEquals(expected, out);
  }

  @Test
  public void testSaveImage() throws Exception {
    Frame f = parseFrame("_waiting.frame");
    doProcess(new DMDScoreProcessorImageDump(), f, "testSaveImage");
  }

  @Test
  public void testScanImage() throws Exception {
    Frame f = parseFrame("_waiting.frame");
    String txt = doProcess(new DMDScoreScannerTessAPI(false), f, "testScanImage");
    assertEquals("WAITING FOR\nSWITCH SCANNING", txt);
  }

  @Test
  public void testBlankIndex() {
    DMDScoreProcessorBase p = new DMDScoreProcessorImageDump();
    assertEquals(0, p.getBlankIndex(parsePalette("[0,16711680,0,0]")));
    assertEquals(1, p.getBlankIndex(parsePalette("[4868863,0,7829367,0]")));
    assertEquals(1, p.getBlankIndex(parsePalette("[16767492,0,16711680,151]")));
    assertEquals(2, p.getBlankIndex(parsePalette("[16760962,8405056,0,11310948]")));
    assertEquals(0, p.getBlankIndex(parsePalette("[20736,16777215,65280,0]")));
    assertEquals(0, p.getBlankIndex(parsePalette("[20736,16777215,65280,34589]")));
  }

  //----------------------

  @Test
  public void testSplitAndScan() throws Exception {
    doTestFrame("_2sides_1.frame", 
            "797,020\n" +
            "650,700\n\n" +
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
            "650,700\n\n" +
            //------------------------
            "FINAL BATTLE CHAMPION\n" + 
            "LFS\n" +
            "25,000,000\n"
    );
  }

  @Test
  public void testSplitAndScanInvertedPalette() throws Exception {
    doTestFrame("_2sides_invertedpalette.frame", 
            "797,020\n" +
            "650,700\n\n" +
            //------------------------
            "FINAL BATTLE CHAMPION\n" + 
            "LFS\n" +
            "25,000,000\n"
    );
  }

  

  @Test
  public void testFrame0() throws Exception {
    doTestFrame("_frame0.frame",  
            "00\n\n" +
            //------------------------
            "00\n" +
            "BALL 1 CREDITS 0\n"
    );
  }

  @Test
  public void testFrameWithImage() throws Exception {
    doTestFrame("_withimage_1.frame",
            "00\n" +
            "00\n" +
            "00\n" +
            "1\n" +                 // 1 in small blue circle detected !
            "BALL 1 CREDITS 0\n"
    );
  }

  @Test
  public void testFrameWithImage2() throws Exception {
    //FIXME some artefacts on frame after removal of images prevent a good recognition
    doTestFrame("_withimage_2.frame",
            "168,020\n" +
            ">\n" +  // should be "00\n" +
            "00\n" +
            "BALL 1\nCREDITS,O"  // should be "BALL 1 CREDITS 0\n"
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
            "15,000\n" + //
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
            "1500000\n" + 
            "BALL 1 CREDITS 0"
    );
  }



  

  //----------------------

  protected void doTestFrame(String frameFile, String expected) throws Exception {
    Frame f = parseFrame(frameFile);
    DMDScoreSplitAndScan proc = new DMDScoreSplitAndScan();
    String txt = doProcess(proc, f, StringUtils.substringBefore(frameFile, ".")); 
    assertEquals(expected, txt);
  }
  
  private String doProcess(DMDScoreProcessor proc, Frame frame, String testname) {
    try {
      DMDScoreScannerTessAPI.TESSERACT_FOLDER = "." + DMDScoreScannerTessAPI.TESSERACT_FOLDER;
      proc.onFrameStart(gameName);
      long timeStart = System.currentTimeMillis();
      String ret = proc.onFrameReceived(frame);
      long time = System.currentTimeMillis() - timeStart;
      System.out.println("test " + testname + " took " + time);
      return ret;
    }
    finally {
      proc.onFrameStop(gameName);
      DMDScoreScannerTessAPI.TESSERACT_FOLDER = DMDScoreScannerTessAPI.TESSERACT_FOLDER.substring(1);
    }
  }

  public Frame parseFrame(String frameName) throws Exception{
    File f = new File(getClass().getResource(frameName).toURI());
    List<String> lines = Files.readLines(f, Charset.forName("UTF-8")); 
    
    String line = lines.get(0);
    String[] parts = StringUtils.splitByWholeSeparator(line, " / ");


    byte[] plane = new byte[w * h];
    int off = 0;
    for (int y = 0; y < h; y++) {
      line = lines.get(y + 1);
      for (int x = 0; x < w; x++) {
        String c = line.substring(x, x+1);
        plane[off++] = " ".equals(c)? 0 : "A".compareTo(c) > 0? Byte.parseByte(c) : (byte) (c.charAt(0) - 55);
      }
    }

    int[] palette = parts.length > 2 ? parsePalette(parts[2]): buildPalette(4);

    // timestamp relative to 2024/01/01
    return new Frame(FrameType.getEnum(parts[0]), "", Integer.parseInt(parts[1]), plane, palette, w, h);
  }

  private int[] parsePalette(String paletteStr) {
    String[] colors = StringUtils.split(paletteStr, "[], ");
    int[] palette = new int[colors.length];
    for (int i = 0; i < colors.length; i++) {
      palette[i] = Integer.parseInt(colors[i]);
    }
    return palette;
  }

  private int[] buildPalette(int size) {
    int [] palette = new int[size];
    for (int i = 0; i < size; i++) {
      int colorComponent = i * 255 / size;
      palette[i] = (255 << 24) | (colorComponent << 16) | (colorComponent << 8) | colorComponent;
    }
    return palette;
  }
}