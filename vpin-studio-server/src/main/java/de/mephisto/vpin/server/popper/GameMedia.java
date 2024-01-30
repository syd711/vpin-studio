package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.restclient.popper.PopperScreen;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameMedia {

  private Map<String, List<GameMediaItem>> media = new HashMap<>();

  public Map<String, List<GameMediaItem>> getMedia() {
    return media;
  }

  @NonNull
  public List<GameMediaItem> getMediaItems(@NonNull PopperScreen screen) {
    return media.get(screen.name());
  }

  @Nullable
  public GameMediaItem getDefaultMediaItem(@NonNull PopperScreen screen) {
    if (!media.containsKey(screen.name())) {
      return null;
    }

    List<GameMediaItem> gameMediaItems = media.get(screen.name());
    if (media.isEmpty()) {
      return null;
    }

    GameMediaItem fallback = null;
    for (GameMediaItem gameMediaItem : gameMediaItems) {
      fallback = gameMediaItem;
      if (gameMediaItem.getName().contains("(SCREEN")) {
        return gameMediaItem;
      }
    }

    return fallback;
  }

  public GameMediaItem getMediaItem(@NonNull PopperScreen screen, @NonNull String name) {
    if (!media.containsKey(screen.name())) {
      return null;
    }

    List<GameMediaItem> gameMediaItems = media.get(screen.name());
    for (GameMediaItem gameMediaItem : gameMediaItems) {
      //TODO mpf
      if (gameMediaItem.getName().equalsIgnoreCase(name) || gameMediaItem.getName().replaceAll(" ",  "+").equalsIgnoreCase(name)) {
        return gameMediaItem;
      }
    }

    return null;
  }
}
