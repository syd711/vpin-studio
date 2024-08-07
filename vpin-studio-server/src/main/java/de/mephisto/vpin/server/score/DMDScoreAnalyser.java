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

  private int timestampStarted = -1;

  @Override
  public void onFrameStart(String gameName) {
    super.onFrameStart(gameName);
    try {
      File file = new File(folder, "recognized.txt");
      this.writer = new BufferedWriter(new FileWriter(file));
      writer.append(gameName + "\n\n");
    } 
    catch (IOException ioe) {
    }
  }

  @Override
  public String onFrameReceived(Frame frame) {
    try {
      writer.append("\n" + frame.getTimeStamp() + "\n");
      String ret = super.onFrameReceived(frame);
      writer.flush();
      return ret;
    }
    catch (IOException ioe) {
      LOG.error("error while writing frame");
      return null;
    }
  }

  protected String extractText(Frame frame, int xFrom, int xTo, int yFrom, int yTo) {
    String txt = super.extractText(frame, xFrom, xTo, yFrom, yTo);
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
