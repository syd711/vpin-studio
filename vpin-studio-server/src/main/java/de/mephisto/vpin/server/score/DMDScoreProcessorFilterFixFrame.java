package de.mephisto.vpin.server.score;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DMDScoreProcessorFilterFixFrame extends DMDScoreProcessorDelegate {

  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreProcessorFilterFixFrame.class);

  /** The difference between 2 frames so that the frame is processed. If -1, consider all */
  private int timestampThreshold;

  public DMDScoreProcessorFilterFixFrame(DMDScoreProcessor... delegates) {
    this(250, delegates);
  }
  public DMDScoreProcessorFilterFixFrame(int timestampThreshold, DMDScoreProcessor... delegates) {
    super(delegates);
    this.timestampThreshold = timestampThreshold;
  }

  public void onFrameReceived(Frame frame, List<FrameText> texts) {
    // consider previous plane only if it has been displayed more than threshold
    if ((timestampThreshold < 0 || (frame.getDisplayTime() > timestampThreshold))) {
      //LOG.info("process {}, timestamp: {}, delta {}", frame.getType(), frame.getTimeStamp(), frame.getDisplayTime());
      super.onFrameReceived(frame, texts);
    }
    else {
      //blinkingFrame = frame;
      //LOG.info("Skipping frame of type: {}", frame.getType());      
    }
  }
}
