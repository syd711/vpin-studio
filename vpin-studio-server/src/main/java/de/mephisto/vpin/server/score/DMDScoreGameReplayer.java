package de.mephisto.vpin.server.score;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;

public class DMDScoreGameReplayer {

  private int w = 128;
  private int h = 32;

  public void replay(DMDScoreGameProcessor processor, InputStream in, int timeFrom, int timeTo) throws Exception{

    String gameName = null;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
      gameName = reader.readLine();
      if (gameName != null) {
        processor.processGameStart(gameName);
        Frame frame;
        while ((frame = readOneFrame(reader)) != null) {
          // skip all frames post timeTo
          if (timeTo >= 0 && frame.getTimeStamp() > timeTo) {
            return;
          }
          // skip frame before timeFrom, but keep iteration
          if (timeFrom < 0 || frame.getTimeStamp() >= timeFrom) {
            processor.processFrame(frame);
          }
        }
      }
    }
    finally {
      if (gameName != null) {
        processor.processGameStop();
      }

      DMDScoreScannerTessAPI.TESSERACT_FOLDER = DMDScoreScannerTessAPI.TESSERACT_FOLDER.substring(1);
    }
  }

  public Frame readOneFrame(BufferedReader reader) throws IOException {
    FrameType ft = null;
    int timestamp = -1;
    int[] palette = null;
    byte[] plane = new byte[0];
    int nbLines = -1;
    
    String line;
    while ((line = reader.readLine()) != null && nbLines < h) {
      // skip white lines
      if (StringUtils.isEmpty(line)) {
        continue;
      }

      // new frame detected
      if (nbLines < 0 && line.contains(" / ")) {
        String[] parts = StringUtils.splitByWholeSeparator(line, " / ");

        ft = FrameType.getEnum(parts[0].trim());
        timestamp =  Integer.parseInt(parts[1].trim());
        palette = parts.length > 2 ? parsePalette(parts[2]): buildPalette(4);
        plane = new byte[w * h];
        nbLines = 0;
        continue;
      }
      // else   
      for (int x = 0; x < w; x++) {
        String c = line.substring(x, x+1);
        plane[nbLines * w + x] = " ".equals(c)? 0 : "A".compareTo(c) > 0? Byte.parseByte(c) : (byte) (c.charAt(0) - 55);
      }
      nbLines++;
    }
    // return the Frame only if it has been read completely
    return nbLines == h? new Frame(ft, timestamp, plane, palette, w, h) : null;
  }

  public int[] parsePalette(String paletteStr) {
    String[] colors = StringUtils.split(paletteStr, "[], ");
    int[] palette = new int[colors.length];
    for (int i = 0; i < colors.length; i++) {
      palette[i] = Integer.parseInt(colors[i]);
    }
    return palette;
  }

  public int[] buildPalette(int size) {
    int [] palette = new int[size];
    for (int i = 0; i < size; i++) {
      int colorComponent = i * 255 / size;
      palette[i] = (255 << 24) | (colorComponent << 16) | (colorComponent << 8) | colorComponent;
    }
    return palette;
  }
}
