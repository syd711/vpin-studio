package de.mephisto.vpin.restclient.frontend;

import java.io.File;

public class PlaylistFrontendMediaItem extends FrontendMediaItem {

  public PlaylistFrontendMediaItem(int gameId, VPinScreen screen, File file) {
    super(gameId, screen, file);
    setUri("playlistmedia/" + gameId + "/" + screen.name());
  }
}
