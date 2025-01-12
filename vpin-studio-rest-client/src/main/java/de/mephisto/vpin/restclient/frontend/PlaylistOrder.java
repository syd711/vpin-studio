package de.mephisto.vpin.restclient.frontend;

import java.util.HashMap;
import java.util.Map;

public class PlaylistOrder {

  private Map<Integer, Integer> playlistToOrderId = new HashMap<>();

  public Map<Integer, Integer> getPlaylistToOrderId() {
    return playlistToOrderId;
  }

  public void setPlaylistToOrderId(Map<Integer, Integer> playlistToOrderId) {
    this.playlistToOrderId = playlistToOrderId;
  }
}
