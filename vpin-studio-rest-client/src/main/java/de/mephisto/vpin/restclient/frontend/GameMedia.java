package de.mephisto.vpin.restclient.frontend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameMedia {

  private final Map<String, List<GameMediaItem>> media = new HashMap<>();

  public Map<String, List<GameMediaItem>> getMedia() {
    return media;
  }

  public List<GameMediaItem> getMediaItems(VPinScreen screen) {
    return media.get(screen.name());
  }

  public GameMediaItem getDefaultMediaItem( VPinScreen screen) {
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

  public GameMediaItem getMediaItem(VPinScreen screen, String name) {
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
