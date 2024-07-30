package de.mephisto.vpin.server.score;

public interface DMDScoreProcessor {
  
  void onFrameStart(String gameName);

  String onFrameReceived(Frame frame, int[] palette, int width, int height);

  default void onFrameStop(String gameName) {}

}
