package de.mephisto.vpin.server.frontend;

import java.io.File;
import java.util.List;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.playlists.Playlist;
import de.mephisto.vpin.restclient.frontend.VPinScreen;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface MediaAccessStrategy {

  File getEmulatorMediaFolder(@NonNull GameEmulator emulator, @NonNull VPinScreen screen);

  File getPlaylistMediaFolder(@NonNull Playlist playlist, @NonNull VPinScreen screen);

  File getGameMediaFolder(@NonNull Game game, VPinScreen screen, String extension);

  List<File> getScreenMediaFiles(@NonNull Game game, @NonNull VPinScreen screen);

}
