package de.mephisto.vpin.server.frontend.popper;

import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.server.frontend.DefaultMediaAccessStrategy;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.playlists.Playlist;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class PinUPMediaAccessStrategy extends DefaultMediaAccessStrategy {

  private final PinUPConnector pinUPConnector;

  public PinUPMediaAccessStrategy(PinUPConnector pinUPConnector) {
    this.pinUPConnector = pinUPConnector;
  }

  @Override
  public File getEmulatorMediaFolder(@NotNull EmulatorType emulatorType) {
    File defaultMedia = new File(pinUPConnector.getSettings().getGlobalMediaDir());
    return new File(defaultMedia.getParentFile(), emulatorType.folderName());
  }

  @Override
  public File getGameMediaFolder(@NonNull Game game, @NonNull VPinScreen screen, @Nullable String extension, boolean create) {
    String mediaDirectory = game.getEmulator().getMediaDirectory();
    File mediaFile = new File(mediaDirectory, screen.name());
    return ensureDirExist(mediaFile, create);
  }

  @Override
  public boolean deleteMedia(@NotNull Game game, @NotNull VPinScreen screen) {
    String mediaDirectory = game.getEmulator().getMediaDirectory();
    File screenFolder = new File(mediaDirectory, screen.name());
    if (screenFolder.exists()) {
      File[] files = screenFolder.listFiles();
      for (File file : files) {
        FileUtils.delete(file);
      }
    }
    return true;
  }

  @Override
  public boolean deleteMedia(@NotNull Playlist playlist, @NotNull VPinScreen screen) {
    File defaultMedia = new File(pinUPConnector.getSettings().getGlobalMediaDir());
    File playlistScreen = new File(defaultMedia, screen.getSegment());
    if (playlistScreen.exists()) {
      File[] files = playlistScreen.listFiles();
      for (File file : files) {
        FileUtils.delete(file);
      }
    }
    return true;
  }

  @Override
  public File getEmulatorMediaFolder(@NonNull GameEmulator emu, @NonNull VPinScreen screen) {
    String mediaDirectory = emu.getMediaDirectory();
    return new File(mediaDirectory, screen.name());
  }

  @Override
  public File getPlaylistMediaFolder(@NonNull Playlist playlist, @NonNull VPinScreen screen, boolean create) {
    File defaultMedia = new File(pinUPConnector.getSettings().getGlobalMediaDir());
    return ensureDirExist(new File(defaultMedia, screen.getSegment()), create);
  }
}
