package de.mephisto.vpin.server.score;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple Analyser that writes recognized text in a file
 */
public class DMDScoreAnalyser extends DMDScoreSplitAndScan {

  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreAnalyser.class);

  private Writer writer;

  @Override
  public void onFrameStart(String gameName) {
    try {
      File file = new File("c:/temp/" + gameName + "/recognized.txt");
      file.getParentFile().mkdirs(); 
      this.writer = new BufferedWriter(new FileWriter(file));
      writer.append(gameName + "\n\n");
    } 
    catch (IOException ioe) {
    }
    super.onFrameStart(gameName);
  }

  @Override
  public String onFrameReceived(Frame frame, int[] palette) {
    try {
      writer.append("\n" + frame.getTimeStamp() + "\n");
    }
    catch (IOException ioe) {
      LOG.error("error while writing frame");
    }
    return super.onFrameReceived(frame, palette);
  }

  protected String extractRect(Frame frame, byte[] plane, int width, int height, int xFrom, int xTo, int yFrom, int yTo) {
    String txt = super.extractRect(frame, plane, width, height, xFrom, xTo, yFrom, yTo);
    try {
      writer.append(xFrom + "," + yFrom + " " 
        + (xTo-xFrom) + "x" + (yTo - yFrom)
        + ": " + txt + "\n");
    }
    catch (IOException ioe) {
      LOG.error("error while writing text " + txt);
    }
    return txt;
  }

  @Override
  public void onFrameStop(String gameName) {
    try {
      this.writer.close();
    }
    catch (IOException ioe) {
      LOG.error("error while opening writer");
    }
    super.onFrameStop(gameName);
  }


}
