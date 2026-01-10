package de.mephisto.vpin.server.dmdscore;

public interface DMDScoreProcessor {
  
  void onFrameStart(String gameName);

  void onFrameReceived(Frame frame, int[] palette, int width, int height);

  default void onFrameStop(String gameName) {}

}
