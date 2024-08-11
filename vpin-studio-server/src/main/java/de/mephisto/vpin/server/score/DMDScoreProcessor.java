package de.mephisto.vpin.server.score;

import java.util.List;

public interface DMDScoreProcessor {
  
  default void onFrameStart(String gameName) {}

  void onFrameReceived(Frame frame, List<FrameText> texts);

  default void onFrameStop(String gameName) {}

}
