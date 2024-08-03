package de.mephisto.vpin.server.score;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

public class DMDScoreReplayTest {

  private int w = 128;
  private int h = 32;

  @Test
  public void testAVR() throws Exception {

    File f = new File(getClass().getResource("avr_200.txt").toURI());

    DMDScoreProcessor proc = new DMDScoreProcessorFilterFixFrame(
      new DMDScoreProcessorImageDump()
      , new DMDScoreAnalyser()
    );

    // skip frames that are strictly equals
    //replayFile(f, proc, 5502975, 5503615);

    //replayFile(f, proc, 5497114, 5502506);

    replayFile(f, proc, -1, -1);
    
  }

  public void replayFile(File file, DMDScoreProcessor processor, int timeFrom, int timeTo) throws Exception{
    DMDScoreScannerBase.TESSERACT_FOLDER = "." + DMDScoreScannerBase.TESSERACT_FOLDER;

    String gameName = null;
    try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {

      int[] palette = buildPalette(16);

      FrameType ft = null;
      int timestamp = -1;
      byte[] plane = new byte[0];

      int nbLines = -1;
      
      
      String line;
      while ((line = reader.readLine()) != null) {
        // skip white lines
        if (StringUtils.isEmpty(line)) {
          continue;
        }

        // new game
        if (gameName == null) {
          gameName = line;
          processor.onFrameStart(gameName);
          continue;
        }
        // new frame
        if (nbLines < 0 && line.contains(" / ")) {
          // new frame detected
          String frameTypeString = StringUtils.substringBefore(line, " / ").trim();
          ft = FrameType.getEnum(frameTypeString);
          timestamp = Integer.parseInt(StringUtils.substringAfter(line, " / ").trim());
          // skip all frames post timeTo
          if (timeTo > 0 && timestamp > timeTo) {
            return;
          }
          nbLines = 0;
          plane = new byte[w * h];
          continue;
        }
        // else
        if (timeFrom < 0 || timestamp >= timeFrom) {
          for (int x = 0; x < w; x++) {
            String c = line.substring(x, x+1);
            plane[nbLines * w + x] = " ".equals(c)? 0 : "A".compareTo(c) > 0? Byte.parseByte(c) : (byte) (c.charAt(0) - 55);
          }
          // last line of the frame
          if (nbLines == h - 1) {
            Frame frame = new Frame(ft, "", timestamp, plane, w, h);
            processor.onFrameReceived(frame, palette);
          }
        }
        // read next frame
        nbLines++;
        if (nbLines == h) {
          nbLines = -1;
        }
      }
    }
    finally {
      processor.onFrameStop(gameName);
      DMDScoreScannerBase.TESSERACT_FOLDER = DMDScoreScannerBase.TESSERACT_FOLDER.substring(1);
    }
  }


  private int[] buildPalette(int size) {
    int [] palette = new int[size];
    for (int i = 0; i < size; i++) {
      int colorComponent = i == 0 ? 255 : i * 255 / size;
      palette[i] = (colorComponent << 16) | (colorComponent << 8) | colorComponent;
    }
    return palette;
  }
}