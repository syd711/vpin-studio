package de.mephisto.vpin.server.frontend;

import java.io.File;
import java.util.List;

import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.playlists.Playlist;
import de.mephisto.vpin.restclient.frontend.VPinScreen;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface MediaAccessStrategy {

  File getEmulatorMediaFolder(@NonNull EmulatorType emulatorType);

  File getEmulatorMediaFolder(@NonNull GameEmulator emulator, @NonNull VPinScreen screen);

  File getPlaylistMediaFolder(@NonNull Playlist playlist, @NonNull VPinScreen screen, boolean create);

  File getGameMediaFolder(@NonNull Game game, @NonNull VPinScreen screen, @Nullable String extension, boolean create);

  List<File> getScreenMediaFiles(@NonNull Game game, @NonNull VPinScreen screen, @Nullable String mediaSearchTerm);

  void stopMonitoring();

}
