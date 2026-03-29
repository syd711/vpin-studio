package de.mephisto.vpin.server.dmdscore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple Processor that writes frames in a file
 */
public class DMDScoreProcessorFrameDump implements DMDScoreProcessor {

  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreProcessorFrameDump.class);

  /** Th eroot folder */
  private File root;

  private Writer writer;


  public DMDScoreProcessorFrameDump(File root) {
    this.root = root;
  }


  @Override
  public void onFrameStart(String gameName) {
    try {
      File file = new File(root, gameName);
      file.mkdirs(); 
      file = new File(file, "dump.txt");
      this.writer = new BufferedWriter(new FileWriter(file));
      writer.append(gameName + "\n\n");
    } 
    catch (IOException ioe) {
    }
  }

  @Override
  public void onFrameReceived(Frame frame) {
    try {
      String txt = frameToString(frame);
      writer.write(txt);
      writer.flush();
    }
    catch (IOException ioe) {
      LOG.error("error while writing frame");
    }
  }

  public static String  frameToString(Frame frame) {
    if (frame == null) {
      return null;
    }

    StringBuilder writer = new StringBuilder();

    int width = frame.getWidth();
    int height = frame.getHeight();

    writer.append(frame.getTimeStamp() + " / " + frame.getType() + " / " + width + " / " + height + "\n");

    int[] plane  = frame.getPlane();
    for (int j = 0; j < height; j++) {
      for (int i = 0; i < width; i++) {
        int color = plane[j * width + i];
        writer.append(brightnessToChar(color, UNICODE_RAMP));
      }
      writer.append("\n");
    }
    writer.append("\n");
    return writer.toString();
  }

  // Characters ordered from darkest to lightest (denser = darker)
  private static final char[] ASCII_CHARS = {
      '@', '#', 'S', '%', '?', '*', '+', ';', ':', ',', '.', ' '
  };
// Extended Unicode density ramp using geometric/box-draw characters
  private static final char[] UNICODE_RAMP = {
      '█','▓','▒','░','▪','·',' '
  };

  /** Maps a brightness value to an ASCII character. */
  private static char brightnessToChar(int rgb, char[] chars) {
    int r = (rgb >> 16) & 0xFF;
    int g = (rgb >>  8) & 0xFF;
    int b =  rgb        & 0xFF;
    // Perceptual luminance (Rec. 709)
    double brightness = 1 - (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255;

    int index = (int) (brightness * (chars.length - 1));
    index = Math.max(0, Math.min(chars.length - 1, index));
    return chars[index];
  }

  @Override
  public void onFrameStop(String gameName) {
    try {
      this.writer.close();
    }
    catch (IOException ioe) {
      LOG.error("error while opening writer");
    }
  }
}
