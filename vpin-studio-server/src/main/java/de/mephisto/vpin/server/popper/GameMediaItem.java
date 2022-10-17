package de.mephisto.vpin.server.popper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class GameMediaItem {
  private String mimeType;
  private String uri;
  private File file;

  public GameMediaItem(@NonNull Game game, @NonNull PopperScreen screen, @NonNull File file) throws IOException {
    this.file = file;
    this.mimeType = Files.probeContentType(file.toPath());
    if(this.mimeType == null) {
      String suffix = FilenameUtils.getExtension(file.getName()).toLowerCase();
      switch (suffix) {
        case "apng": {
          this.mimeType = "image/apng";
          break;
        }
      }
    }

    this.uri = "poppermedia/" + game.getId() + "/" + screen.name();
  }

  @JsonIgnore
  public File getFile() {
    return file;
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
