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

    int[] palette = frame.getPalette();
    for (int p = 0; p < palette.length; p++) {
      if (p != 0) writer.append(",");
      writer.append(Integer.toString(palette[p]));
    }
    writer.append("\n");

    byte[] plane  = frame.getPlane();
    for (int j = 0; j < height; j++) {
      for (int i = 0; i < width; i++) {
        int idx = plane[j * width + i];
        writer.append(idx == 0? " " : idx<=9 ? Integer.toString(idx) : Character.toString(55 + idx));
      }
      writer.append("\n");
    }
    writer.append("\n");
    return writer.toString();
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
