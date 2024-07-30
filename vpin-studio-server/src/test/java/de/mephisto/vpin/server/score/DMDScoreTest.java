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
    Frame f = parseFrame("waiting.frame");
    String out = doProcess(new DMDScoreProcessorFrameDump(), f);

    File file = new File(getClass().getResource("waiting.frame").toURI());
    String expected = FileUtils.readFileToString(file, "UTF-8");
    expected = expected.replace(System.lineSeparator(), "\n");

    assertEquals(expected, out);
  }

  @Test
  public void testSaveImage() throws Exception {
    Frame f = parseFrame("waiting.frame");
    doProcess(new DMDScoreProcessorImageDump(), f);
  }

  @Test
  public void testScanImage() throws Exception {
    Frame f = parseFrame("waiting.frame");
    String txt = doProcess(new DMDScoreProcessorImageScanner(), f);
    assertEquals("WAITING FOR\nSWITCH SCANNING\n", txt);
  }

  //----------------------

  private String doProcess(DMDScoreProcessor proc, Frame frame) {
    try {
      DMDScoreProcessorImageScanner.TESSERACT_FOLDER = "." + DMDScoreProcessorImageScanner.TESSERACT_FOLDER;
      int[] palette = buildPalette(4);
      proc.onFrameStart(gameName);
      return proc.onFrameReceived(frame, palette, w, h);
    }
    finally {
      proc.onFrameStop(gameName);
      DMDScoreProcessorImageScanner.TESSERACT_FOLDER = DMDScoreProcessorImageScanner.TESSERACT_FOLDER.substring(1);
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

  public Frame parseFrame(String frameName) throws Exception{
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
    return new Frame(FrameType.COLORED_GRAY_2, 12345, plane);
  }
}