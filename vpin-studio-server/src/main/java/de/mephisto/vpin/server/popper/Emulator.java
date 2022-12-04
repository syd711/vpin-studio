package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class Emulator {
  public final static String VISUAL_PINBALL_X = "Visual Pinball X";
  public final static String FUTURE_PINBALL = "Future Pinball";

  private final Game game;

  private String name;
  private int id;
  private String mediaDir;

  public Emulator(Game game) {
    this.game = game;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getMediaDir() {
    return mediaDir;
  }

  public void setMediaDir(String mediaDir) {
    this.mediaDir = mediaDir;
  }

  @NonNull
  public File getPinUPMediaFolder(@NonNull PopperScreen screen) {
    File emulatorMediaFolder = new File(this.mediaDir);
    return new File(emulatorMediaFolder, screen.name());
  }

  @Nullable
  public File getPinUPMedia(@NonNull PopperScreen screen) {
    String baseName = FilenameUtils.getBaseName(game.getGameFileName());
    File[] mediaFiles = getPinUPMediaFolder(screen).listFiles((dir, name) -> FilenameUtils.getBaseName(name).equals(baseName));
    if (mediaFiles != null && mediaFiles.length > 0) {
      return mediaFiles[0];
    }
    return null;
  }

  @NonNull
  public GameMedia getGameMedia() {
    GameMedia gameMedia = new GameMedia();
    PopperScreen[] screens = PopperScreen.values();
    for (PopperScreen screen : screens) {
      File mediaFile = getPinUPMedia(screen);
      if (mediaFile != null) {
        GameMediaItem item = new GameMediaItem(game, screen, mediaFile);
        gameMedia.getMedia().put(screen.name(), item);
      }
    }
    return gameMedia;
  }
}
