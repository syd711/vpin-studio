package de.mephisto.vpin.server.score;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DMDScoreProcessorFilterFixFrame extends DMDScoreProcessorDelegate {

  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreProcessorFilterFixFrame.class);

  private int threshold;

  private Frame previousFrame;
  private Frame blinkingFrame;
  private int[] previousPalette;

  public DMDScoreProcessorFilterFixFrame(DMDScoreProcessor... delegates) {
    this(250, delegates);
  }
  public DMDScoreProcessorFilterFixFrame(int threshold, DMDScoreProcessor... delegates) {
    super(delegates);
    this.threshold = threshold;
  }

  public String onFrameReceived(Frame frame, int[] palette) {
    String ret = null;
    if (previousFrame != null && frame.equals(previousFrame)) {
      LOG.info("Skipping duplicate frame of type: {}", frame.getType());
      blinkingFrame = null;
    }
    else {
      // consider previous plane only if it has been displayed more than threshold
      if (previousFrame != null && (frame.getTimeStamp() - previousFrame.getTimeStamp() > threshold)) {
        LOG.info("process {}, timestamp: {}, delta {}", frame.getType(), frame.getTimeStamp(), frame.getTimeStamp() - previousFrame.getTimeStamp());
        ret = super.onFrameReceived(previousFrame, previousPalette);
      }
      else {
        //blinkingFrame = frame;
        //LOG.info("Skipping frame of type: {}", frame.getType());
      
      }

      previousFrame = frame;
      previousPalette = palette;
    }
    return ret;
  }
}
