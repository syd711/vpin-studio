package de.mephisto.vpin.server.score;

import java.util.List;

public class DMDScoreProcessorDelegate implements DMDScoreProcessor {
  
  private DMDScoreProcessor[] delegates;

  public DMDScoreProcessorDelegate(DMDScoreProcessor... delegates) {
    this.delegates = delegates;
  }

  @Override
  public void onFrameStart(String gameName) {
    for (DMDScoreProcessor delegate: delegates) {
      delegate.onFrameStart(gameName);
    }
  }

  @Override
  public void onFrameReceived(Frame frame, List<FrameText> texts) {
    for (DMDScoreProcessor delegate: delegates) {
      delegate.onFrameReceived(frame, texts);
    }
  }

  @Override
  public void onFrameStop(String gameName) {
    for (DMDScoreProcessor delegate: delegates) {
      delegate.onFrameStop(gameName);
    }
  }
}
