package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.restclient.PopperScreen;

import java.util.HashMap;
import java.util.Map;

public class GameMedia {

  private Map<String, GameMediaItem> media = new HashMap<>();

  public Map<String, GameMediaItem> getMedia() {
    return media;
  }

  public GameMediaItem get(PopperScreen screen) {
    return media.get(screen.name());
  }
}
