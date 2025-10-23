package de.mephisto.vpin.restclient.games;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;

public class FrontendMediaRepresentation {
  private Map<String, List<FrontendMediaItemRepresentation>> media = new HashMap<>();

  public Map<String, List<FrontendMediaItemRepresentation>> getMedia() {
    return media;
  }

  public void setMedia(Map<String, List<FrontendMediaItemRepresentation>> media) {
    this.media = media;
  }

  public List<FrontendMediaItemRepresentation> getMediaItems(VPinScreen screen) {
    if (media.containsKey(screen.name())) {
      List<FrontendMediaItemRepresentation> mediaItemRepresentations = this.media.get(screen.name());
      // compare filenames ignoring case
      Collections.sort(mediaItemRepresentations, (i1, i2) -> i1.getName().compareToIgnoreCase(i2.getName()));
      return mediaItemRepresentations;
    }
    return Collections.emptyList();
  }

  @Nullable
  public FrontendMediaItemRepresentation getDefaultMediaItem(@NonNull VPinScreen screen) {
    if (!media.containsKey(screen.name())) {
      return null;
    }

    List<FrontendMediaItemRepresentation> gameMediaItems = media.get(screen.name());
    if (media.isEmpty()) {
      return null;
    }

    FrontendMediaItemRepresentation fallback = null;
    for (FrontendMediaItemRepresentation gameMediaItem : gameMediaItems) {
      if (fallback == null) {
        // use first media as default
        fallback = gameMediaItem;
      }
      if (gameMediaItem.getName().contains("[SCREEN")) {
        return gameMediaItem;
      }
    }

    return fallback;
  }
}
