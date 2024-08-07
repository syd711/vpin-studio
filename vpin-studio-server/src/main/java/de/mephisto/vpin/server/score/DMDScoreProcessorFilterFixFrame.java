package de.mephisto.vpin.server.score;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DMDScoreProcessorFilterFixFrame extends DMDScoreProcessorDelegate {

  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreProcessorFilterFixFrame.class);

  /** The difference between 2 frames so that the frame is processed. If -1, consider all */
  private int timestampThreshold;

  private Frame previousFrame;


  public DMDScoreProcessorFilterFixFrame(DMDScoreProcessor... delegates) {
    this(250, delegates);
  }
  public DMDScoreProcessorFilterFixFrame(int timestampThreshold, DMDScoreProcessor... delegates) {
    super(delegates);
    this.timestampThreshold = timestampThreshold;
  }

  public String onFrameReceived(Frame frame) {
    String ret = null;
    if (previousFrame != null && frame.equals(previousFrame)) {
      LOG.info("Skipping duplicate frame of type: {}", frame.getType());
    }
    else {
      // consider previous plane only if it has been displayed more than threshold
      if (previousFrame != null && (timestampThreshold < 0 || (frame.getTimeStamp() - previousFrame.getTimeStamp() > timestampThreshold))) {
        LOG.info("process {}, timestamp: {}, delta {}", frame.getType(), frame.getTimeStamp(), frame.getTimeStamp() - previousFrame.getTimeStamp());
        ret = super.onFrameReceived(previousFrame);
      }
      else {
        //blinkingFrame = frame;
        //LOG.info("Skipping frame of type: {}", frame.getType());
      
      }

      previousFrame = frame;
    }
    return ret;
  }

  @Override
  public void onFrameStop(String gameName) {
    // always process the very last frame 
    if (previousFrame != null) {
      super.onFrameReceived(previousFrame);
    }
    super.onFrameStop(gameName);
  }
}
