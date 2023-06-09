package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.restclient.popper.PopperScreen;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class GameMedia {

  private Map<String, GameMediaItem> media = new HashMap<>();

  public Map<String, GameMediaItem> getMedia() {
    return media;
  }

  @Nullable
  public GameMediaItem get(PopperScreen screen) {
    return media.get(screen.name());
  }
}
