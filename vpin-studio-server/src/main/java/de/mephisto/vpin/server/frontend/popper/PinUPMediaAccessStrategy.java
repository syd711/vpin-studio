package de.mephisto.vpin.server.frontend.popper;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.frontend.DefaultMediaAccessStrategy;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.playlists.Playlist;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;

public class PinUPMediaAccessStrategy extends DefaultMediaAccessStrategy {

  private File installationFolder;

  public PinUPMediaAccessStrategy(File installationFolder) {
    this.installationFolder = installationFolder;
  }

  @Override
  public File getGameMediaFolder(@NonNull Game game, VPinScreen screen, String extension) {
    String mediaDirectory = game.getEmulator().getMediaDirectory();
    return new File(mediaDirectory, screen.name());
  }

  @Override
  public File getEmulatorMediaFolder(@NonNull GameEmulator emu, VPinScreen screen) {
    String mediaDirectory = emu.getMediaDirectory();
    return new File(mediaDirectory, screen.name());
  }

  @Override
  public File getPlaylistMediaFolder(@NonNull Playlist playlist, @NonNull VPinScreen screen) {
    File defaultMedia = new File(installationFolder, "POPMedia/Default");
    return new File(defaultMedia, screen.getSegment());
  }

}
