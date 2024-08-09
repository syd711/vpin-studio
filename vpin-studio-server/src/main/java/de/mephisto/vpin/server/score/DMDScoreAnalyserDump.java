package de.mephisto.vpin.server.score;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple Analyser that dump recognized text in a file
 */
public class DMDScoreAnalyserDump implements DMDScoreProcessor {

  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreAnalyserDump.class);

  private Writer writer;

  @Override
  public void onFrameStart(String gameName) {
    try {
      File file = new File("c:/temp/" + gameName, "recognized.txt");
      this.writer = new BufferedWriter(new FileWriter(file));
      writer.append(gameName + "\n\n");
    } 
    catch (IOException ioe) {
    }
  }

  @Override
  public void onFrameReceived(Frame frame, List<FrameText> texts) {
    try {
      writer.append("\n" + frame.getTimeStamp() + "\n");
      for (FrameText text : texts) {
        writer.append(text.getX() + "," + text.getY() + " " 
          + text.getWidth() + "x" + text.getHeight()
          + ": " + text.getText() + "\n");
      }
      writer.flush();
    }
    catch (IOException ioe) {
      LOG.error("error while writing frame");
    }
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
