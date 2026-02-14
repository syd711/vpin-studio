package de.mephisto.vpin.restclient.frontend;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrontendMedia {

  private final Map<String, List<FrontendMediaItem>> media = new HashMap<>();

  public Map<String, List<FrontendMediaItem>> getMedia() {
    return media;
  }

  public List<FrontendMediaItem> getMediaItems(VPinScreen screen) {
    if (!media.containsKey(screen.name())) {
      return Collections.emptyList();
    }
    return media.get(screen.name());
  }

  public FrontendMediaItem getDefaultMediaItem(VPinScreen screen) {
    if (screen == null || !media.containsKey(screen.name())) {
      return null;
    }

    List<FrontendMediaItem> frontendMediaItems = media.get(screen.name());
    if (media.isEmpty()) {
      return null;
    }

    FrontendMediaItem fallback = null;
    for (FrontendMediaItem frontendMediaItem : frontendMediaItems) {
      if (fallback == null) {
        fallback = frontendMediaItem;
      }
      if (frontendMediaItem.getName().contains("(SCREEN")) {
        return frontendMediaItem;
      }
    }

    return fallback;
  }

  public FrontendMediaItem getMediaItem(VPinScreen screen, String name) {
    if (!media.containsKey(screen.name())) {
      return null;
    }

    List<FrontendMediaItem> frontendMediaItems = media.get(screen.name());
    for (FrontendMediaItem frontendMediaItem : frontendMediaItems) {
      //TODO mpf
      if (frontendMediaItem.getName().equalsIgnoreCase(name) || frontendMediaItem.getName().replaceAll(" ", "+").equalsIgnoreCase(name)) {
        return frontendMediaItem;
      }
    }

    return null;
  }
}
