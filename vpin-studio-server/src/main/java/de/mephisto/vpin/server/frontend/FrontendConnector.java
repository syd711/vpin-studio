package de.mephisto.vpin.server.frontend;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.playlists.Playlist;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface FrontendConnector {
  /**
   * Let the Frontend initialize itself, called
   */
  void initializeConnector();

  Frontend getFrontend();

  TableAssetsAdapter getTableAssetAdapter();


  @NonNull
  File getInstallationFolder();

  //----------------------------------
  // Emulator Management

  List<GameEmulator> getEmulators();

  GameEmulator getEmulator(int emulatorId);

  boolean deleteEmulator(int emulatorId);

  GameEmulator saveEmulator(GameEmulator emulator);

  /**
   * Force refresh of the whole connector since they can have their own cache, e.g. emulators
   */
  void reloadCache();

  Game getGame(int id);

  @Nullable
  Game getGameByFilename(int emuId, String filename);

  @NonNull
  List<Game> getGamesByEmulator(int emuId);

  @NonNull
  List<Game> getGamesByFilename(String filename);

  @Nullable
  Game getGameByName(int emuId, String gameName);

  int importGame(int emuId, @NonNull String gameName, @NonNull String gameFileName, @NonNull String gameDisplayName, @Nullable String launchCustomVar, @NonNull Date dateFileUpdated);

  boolean deleteGame(int id);

  void deleteGames(int emuId);

  int getGameCount(int emuId);

  List<Integer> getGameIds(int emuId);

  @NonNull
  List<Game> getGames();

  //----------------------------------
  // Specific TableDetails managed in frontend database

  @Nullable
  TableDetails getTableDetails(int id);

  void updateTableFileUpdated(int id);

  void saveTableDetails(int id, TableDetails tableDetails);

  void vpsLink(int id, String extTableId, String extTableVersionId);

  //----------------------------------
  // version and options 

  int getVersion();

  JsonSettings getSettings();

  void saveSettings(@NonNull Map<String, Object> data);

  //----------------------------------
  // Media management

  /**
   * Returns the strategy to access files.
   * By returning a null value, it tells the frontend does not support media
   */
  MediaAccessStrategy getMediaAccessStrategy();

  void setPupPackEnabled(@NonNull Game game, boolean enable);

  List<FrontendPlayerDisplay> getFrontendPlayerDisplays();

  //----------------------------------
  // Playlists management

  @NonNull
  Playlist getPlaylist(int id);

  @NonNull
  Playlist clearPlaylist(int id);

  @NonNull
  List<Playlist> getPlaylists();

  @NonNull
  Playlist getPlaylistTree();

  boolean deletePlaylist(int playlistId);

  Playlist savePlaylist(Playlist playlist);

  void savePlaylistOrder(PlaylistOrder playlistOrder);

  void addToPlaylist(int playlistId, int gameId, int favMode);

  void updatePlaylistGame(int playlistId, int gameId, int favMode);

  void deleteFromPlaylists(int gameId);

  void deleteFromPlaylist(int playlistId, int gameId);
  //----------------------------------

  // Statistics management

  @NonNull
  java.util.Date getStartDate();

  List<TableAlxEntry> getAlxData();

  @NonNull
  List<TableAlxEntry> getAlxData(int gameId);

  boolean updateNumberOfPlaysForGame(int gameId, long value);

  boolean updateSecondsPlayedForGame(int gameId, long seconds);
  //----------------------------------

  // Pinup control management

  FrontendControl getFrontendControlFor(VPinScreen screen);

  FrontendControl getFrontendControl(String function);

  @NonNull
  FrontendControls getControls();

  //----------------------------------
  // UI Management

  boolean killFrontend();

  boolean isFrontendRunning();

  boolean restartFrontend();

  boolean launchGame(Game game);
  //----------------------------------
  // Recording

  boolean startFrontendRecording();

  boolean startGameRecording(Game game);

  void endGameRecording(Game game);

  void endFrontendRecording();
}
