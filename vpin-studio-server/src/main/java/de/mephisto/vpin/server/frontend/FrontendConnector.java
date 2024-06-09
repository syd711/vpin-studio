package de.mephisto.vpin.server.frontend;

import java.util.Date;
import java.util.List;

import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.popper.Emulator;
import de.mephisto.vpin.restclient.popper.PinUPControl;
import de.mephisto.vpin.restclient.popper.PinUPControls;
import de.mephisto.vpin.restclient.popper.Playlist;
import de.mephisto.vpin.restclient.popper.PopperCustomOptions;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface FrontendConnector {
  /** 
   * Let the Frontend initialize itself, called 
   */
  void initialize(ServerSettings settings);

  List<Emulator> getEmulators();
  
  Game getGame(int id);

  @NonNull
  TableDetails getTableDetails(int id);

  void updateTableFileUpdated(int id);

  void saveTableDetails(int id, TableDetails tableDetails);

  @Nullable
  Game getGameByFilename(String filename);

  @NonNull
  List<Game> getGamesByEmulator(int emulatorId);

  @NonNull
  List<Game> getGamesByFilename(String filename);

  @Nullable
  Game getGameByName(String gameName);

  @NonNull
  int getSqlVersion();
  
  boolean isPopper15();

  PopperCustomOptions getCustomOptions();

  void updateCustomOptions(@NonNull PopperCustomOptions options);

  void updateRom(@NonNull Game game, String rom);

  void updateGamesField(@NonNull Game game, String field, String value);

  @Nullable
  String getGamesStringValue(@NonNull Game game, @NonNull String field);

  int importGame(int emulatorId, @NonNull String gameName, @NonNull String gameFileName, @NonNull String gameDisplayName, @Nullable String launchCustomVar, @NonNull Date dateFileUpdated);

  boolean deleteGame(int id);


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


  List<TableAlxEntry> getAlxData();

  @NonNull
  List<TableAlxEntry> getAlxData(int gameId);

  PinUPControl getPinUPControlFor(PopperScreen screen);

  //TODO rename getControl
  PinUPControl getFunction(String function);

  @NonNull
  PinUPControls getControls();


  int getGameCount(int emuId);

  List<Integer> getGameIds(int emuId);

  @NonNull
  List<Game> getGames();

  @NonNull
  java.util.Date getStartDate();

  void deleteGames();

  @NonNull
  List<Integer> getGameIdsFromPlaylists();

  MediaAccessStrategy getMediaAccessStrategy();

}
