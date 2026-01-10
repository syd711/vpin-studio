package de.mephisto.vpin.server.dmdscore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DMDScoreProcessorFilterFixFrame extends DMDScoreProcessorDelegate {

  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreProcessorFilterFixFrame.class);

  private int threshold;

  private Frame previousFrame;
  private int[] previousPalette;

  public DMDScoreProcessorFilterFixFrame(DMDScoreProcessor delegate) {
    this(delegate, 500);
  }
  public DMDScoreProcessorFilterFixFrame(DMDScoreProcessor delegate, int threshold) {
    super(delegate);
    this.threshold = threshold;
  }

  public void onFrameReceived(Frame frame, int[] palette, int width, int height) {
    if (previousFrame == null || !frame.equals(previousFrame)) {
      // consider previous plane only if it has been displayed more than threshold
      if (previousFrame != null && (frame.getTimeStamp() - previousFrame.getTimeStamp() > threshold)) {
        LOG.info("process {}, timestamp: {}, delta {}", frame.getType(), frame.getTimeStamp(), frame.getTimeStamp() - previousFrame.getTimeStamp());
        super.onFrameReceived(previousFrame, previousPalette, width, height);
      }
      else {
        LOG.info("Skipping frame of type: {}", frame.getType());
      }
      previousFrame = frame;
      previousPalette = palette;
    }
    else {
      LOG.info("Skipping duplicate frame of type: {}", frame.getType());
    }
  }
}
