package de.mephisto.vpin.server.dmdscore;

public class DMDScoreProcessorDelegate implements DMDScoreProcessor {
  
  private DMDScoreProcessor delegate;

  public DMDScoreProcessorDelegate(DMDScoreProcessor delegate) {
    this.delegate = delegate;
  }

  @Override
  public void onFrameStart(String gameName) {
    delegate.onFrameStart(gameName);
  }

  @Override
  public void onFrameReceived(Frame frame, int[] palette, int width, int height) {
    delegate.onFrameReceived(frame, palette, width, height);
  }

  @Override
  public void onFrameStop(String gameName) {
    delegate.onFrameStop(gameName);
  }
}
