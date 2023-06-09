package de.mephisto.vpin.restclient.representations;

import de.mephisto.vpin.restclient.popper.PopperScreen;

import java.util.HashMap;
import java.util.Map;

public class GameMediaRepresentation {
  private Map<String, GameMediaItemRepresentation> media = new HashMap<>();

  public Map<String, GameMediaItemRepresentation> getMedia() {
    return media;
  }

  public void setMedia(Map<String, GameMediaItemRepresentation> media) {
    this.media = media;
  }

  public GameMediaItemRepresentation getItem(PopperScreen screen) {
    return this.media.get(screen.name());
  }
}
