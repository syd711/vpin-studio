package de.mephisto.vpin.server.score;

public interface DMDScoreProcessor {
  
  void onFrameStart(String gameName);

  String onFrameReceived(Frame frame);

  default void onFrameStop(String gameName) {}

}
