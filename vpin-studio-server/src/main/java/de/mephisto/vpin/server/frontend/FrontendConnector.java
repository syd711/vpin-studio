package de.mephisto.vpin.server.frontend;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.frontend.Emulator;
import de.mephisto.vpin.restclient.frontend.FrontendControl;
import de.mephisto.vpin.restclient.frontend.FrontendControls;
import de.mephisto.vpin.restclient.frontend.Playlist;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface FrontendConnector {
  /** 
   * Let the Frontend initialize itself, called 
   */
  void initializeConnector(ServerSettings settings);

  @NonNull
  File getInstallationFolder();

  List<Emulator> getEmulators();
  
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

  void deleteGames();
  
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

  //----------------------------------
  // Playlists management

  @NonNull
  Playlist getPlayList(int id);

  @NonNull
  List<Playlist> getPlayLists(boolean excludeSqlLists);

  void setPlaylistColor(int playlistId, long color);

  void addToPlaylist(int playlistId, int gameId, int favMode);

  void updatePlaylistGame(int playlistId, int gameId, int favMode);

  void deleteFromPlaylists(int gameId);

  void deleteFromPlaylist(int playlistId, int gameId);

  Playlist getPlayListForGame(int gameId);

  @NonNull
  List<Integer> getGameIdsFromPlaylists();

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

}
