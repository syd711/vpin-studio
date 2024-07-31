package de.mephisto.vpin.server.score;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import com.google.common.io.Files;

public class DMDScoreTest {

  private String gameName = "test";
  private int w = 128;
  private int h = 32;

  @Test
  public void testSaveFrame() throws Exception {
    Frame f = parseFrame("_waiting.frame", 12345);
    String out = doProcess(new DMDScoreProcessorFrameDump(), f, "testSaveFrame");

    File file = new File(getClass().getResource("_waiting.frame").toURI());
    String expected = FileUtils.readFileToString(file, "UTF-8");
    expected = expected.replace(System.lineSeparator(), "\n");

    assertEquals(expected, out);
  }

  @Test
  public void testSaveImage() throws Exception {
    Frame f = parseFrame("_waiting.frame", 12345);
    doProcess(new DMDScoreProcessorImageDump(), f, "testSaveImage");
  }

  @Test
  public void testScanImage() throws Exception {
    Frame f = parseFrame("_waiting.frame", 12345);
    String txt = doProcess(new DMDScoreScannerCommandLine(), f, "testScanImage");
    assertEquals("WAITING FOR\nSWITCH SCANNING\n", txt);
  }

  @Test
  public void testScan2sides1() throws Exception {
    Frame f = parseFrame("_2sides_1.frame", 12346);
    String txt = doProcess(new DMDScoreProcessor2Sides(41), f, "testScan2sides1");
    
    //FIXME do not recognize simple separated caracters and SCORE
    assertEquals("797,020\n" +
            "650,700\n\n" +
            //------------------------
            "HIGH pCORE #1\n" + 
            //"L R\n" +             
            "55,000,000\n", txt);
  }

  @Test
  public void testScan2sides2() throws Exception {
    Frame f = parseFrame("_2sides_2.frame", 12347);
    String txt = doProcess(new DMDScoreProcessor2Sides(41), f, "testScan2sides2");
    assertEquals("797,020\n" +
            "650,700\n\n" +
            //------------------------
            "FINAL BATTLE CHAMPION\n" + 
            "LFS\n" +
            "25,000,000\n", txt);
  }

  @Test
  public void testSplit2sides2() throws Exception {
    Frame f = parseFrame("_2sides_1.frame", 12348);
    DMDScoreScannerBase proc = new DMDScoreScannerCommandLine();
    DMDScoreProcessorFrameSplitter splitter = new DMDScoreProcessorFrameSplitter(proc);
    String txt = doProcess(splitter, f, "testSplit2sides2"); 
    assertEquals("797,020\n" +
            "650,700\n\n" +
            //------------------------
            "HIGH SCORE #1\n" + 
            "LR\n" +             
            "55,000,000\n", txt);
  }


  @Test
  public void testSplit2sides2JNAInterface() throws Exception {

    Frame f = parseFrame("_2sides_1.frame", 12348);
    DMDScoreScannerTessAPI proc = new DMDScoreScannerTessAPI();
    DMDScoreProcessorFrameSplitter splitter = new DMDScoreProcessorFrameSplitter(proc);
    String txt = doProcess(splitter, f, "testSplit2sides2 JNA Interface"); 
    assertEquals("797,020\n" +
            "650,700\n\n" +
            //------------------------
            "HIGH SCORE #1\n" + 
            "LR\n" +             
            "55,000,000\n", txt);
  }

  @Test
  public void testSplitAndScan() throws Exception {

    Frame f = parseFrame("_2sides_1.frame", 12348);
    DMDScoreSplitAndScan proc = new DMDScoreSplitAndScan();
    String txt = doProcess(proc, f, "testSplitAndScan"); 
    assertEquals("797,020\n" +
            "650,700\n\n" +
            //------------------------
            "HIGH SCORE #1\n" + 
            "LR\n" +             
            "55,000,000\n", txt);
  }

  

  //----------------------

  private String doProcess(DMDScoreProcessor proc, Frame frame, String testname) {
    try {
      DMDScoreScannerBase.TESSERACT_FOLDER = "." + DMDScoreScannerBase.TESSERACT_FOLDER;
      int[] palette = buildPalette(4);
      proc.onFrameStart(gameName);
      long timeStart = System.currentTimeMillis();
      String ret = proc.onFrameReceived(frame, palette);
      long time = System.currentTimeMillis() - timeStart;
      System.out.println("test " + testname + " took " + time);
      return ret;
    }
    finally {
      proc.onFrameStop(gameName);
      DMDScoreScannerBase.TESSERACT_FOLDER = DMDScoreScannerBase.TESSERACT_FOLDER.substring(1);
    }
  }


  private int[] buildPalette(int size) {
    int [] palette = new int[size];
    for (int i = 0; i < size; i++) {
      int colorComponent = i * 255 / size;
      palette[i] = (255 << 24) | (colorComponent << 16) | (colorComponent << 8) | colorComponent;
    }
    return palette;
  }

  public Frame parseFrame(String frameName, int timestamp) throws Exception{
    File f = new File(getClass().getResource(frameName).toURI());
    List<String> lines = Files.readLines(f, Charset.forName("UTF-8")); 
    
    byte[] plane = new byte[w * h];
    int off = 0;
    for (int y = 0; y < h; y++) {
      String line = lines.get(y);
      for (int x = 0; x < w; x++) {
        String c = line.substring(x, x+1);
        plane[off++] = " ".equals(c)? 0 : Byte.parseByte(c);
      }
    }
    // timestamp relative to 2024/01/01
    return new Frame(FrameType.COLORED_GRAY_2, "", timestamp, plane, w, h);
  }
}