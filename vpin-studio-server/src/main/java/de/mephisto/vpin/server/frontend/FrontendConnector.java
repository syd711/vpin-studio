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

  List<Emulator> getEmulators();

  /**
   * Force refresh of the whole connector since they can have their own cache, e.g. emulators
   */
  void clearCache();

  Game getGame(int id);

  @Nullable
  Game getGameByFilename(String filename);

  @NonNull
  List<Game> getGamesByEmulator(int emulatorId);

  @NonNull
  List<Game> getGamesByFilename(String filename);

  @Nullable
  Game getGameByName(String gameName);

  int importGame(int emulatorId, @NonNull String gameName, @NonNull String gameFileName, @NonNull String gameDisplayName, @Nullable String launchCustomVar, @NonNull Date dateFileUpdated);

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

  boolean isPupPackDisabled(@NonNull Game game);

  void setPupPackEnabled(@NonNull Game game, boolean enable);

  List<FrontendPlayerDisplay> getFrontendPlayerDisplays();

  //----------------------------------
  // Playlists management

  @NonNull
  Playlist getPlayList(int id);

  @NonNull
  List<Playlist> getPlayLists();

  void setPlaylistColor(int playlistId, long color);

  void addToPlaylist(int playlistId, int gameId, int favMode);

  void updatePlaylistGame(int playlistId, int gameId, int favMode);

  void deleteFromPlaylists(int gameId);

  void deleteFromPlaylist(int playlistId, int gameId);

  Playlist getPlayListForGame(int gameId);

  @NonNull
  List<Integer> getGameIdsFromPlaylists();

  File getPlaylistMediaFolder(@NonNull Playlist playList, @NonNull VPinScreen screen);

  //----------------------------------
  // Statistics management

  @NonNull
  java.util.Date getStartDate();

  List<TableAlxEntry> getAlxData();

  @NonNull
  List<TableAlxEntry> getAlxData(int gameId);

  //----------------------------------
  // Pinup control management

  FrontendControl getPinUPControlFor(VPinScreen screen);

  //TODO rename getControl
  FrontendControl getFunction(String function);

  @NonNull
  FrontendControls getControls();


  //----------------------------------
  // UI Management
  boolean killFrontend();

  boolean isFrontendRunning();

  boolean restartFrontend();
}