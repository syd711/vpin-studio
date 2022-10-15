package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class GameMediaItem {
  private String mimeType;
  private String uri;

  public GameMediaItem(@NonNull Game game, @NonNull PopperScreen screen, @NonNull File mediaFile) throws IOException {
    this.mimeType = Files.probeContentType(mediaFile.toPath());
    this.uri = "poppermedia/" + game.getId() + "/" + screen.name();
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }
}
