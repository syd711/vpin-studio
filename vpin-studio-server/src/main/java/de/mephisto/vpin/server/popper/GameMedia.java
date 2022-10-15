package de.mephisto.vpin.server.popper;

import java.util.HashMap;
import java.util.Map;

public class GameMedia {

  private Map<String, GameMediaItem> media = new HashMap<>();

  public Map<String, GameMediaItem> getMedia() {
    return media;
  }
}
