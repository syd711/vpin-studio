package de.mephisto.vpin.restclient.frontend;

import java.io.File;

public class PlaylistFrontendMediaItem extends FrontendMediaItem {

  public PlaylistFrontendMediaItem(int gameId, VPinScreen screen, File file) {
    super(gameId, screen, file);
  }

  public String getUri() {
    return "playlistmedia/" + getGameId() + "/" + getScreen();
  }
}
