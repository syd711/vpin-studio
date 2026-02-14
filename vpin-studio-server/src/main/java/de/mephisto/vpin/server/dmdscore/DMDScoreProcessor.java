package de.mephisto.vpin.server.dmdscore;

public interface DMDScoreProcessor {
  
  void onFrameStart(String gameName);

  void onFrameReceived(Frame frame);

  default void onFrameStop(String gameName) {}

}
