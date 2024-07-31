package de.mephisto.vpin.server.score;

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
  public String onFrameReceived(Frame frame, int[] palette) {
    String ret = null;
    for (DMDScoreProcessor delegate: delegates) {
      ret = delegate.onFrameReceived(frame, palette);
    }
    return ret;
  }

  @Override
  public void onFrameStop(String gameName) {
    for (DMDScoreProcessor delegate: delegates) {
      delegate.onFrameStop(gameName);
    }
  }
}
