package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.playlists.Playlist;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseConnector implements FrontendConnector {
  private final static Logger LOG = LoggerFactory.getLogger(BaseConnector.class);

  /**
   * the loaded and cached emulators
   */
  protected Map<Integer, Emulator> emulators = new HashMap<>();
  /**
   * Map by emulator of ids
   */
  protected Map<Integer, List<String>> gamesByEmu = new HashMap<>();
  /**
   * Map id <-> filename
   */
  private Map<Integer, GameEntry> mapFilenames = new HashMap<>();

  class GameEntry {
    GameEntry(int emuId, String filename) {
      this.emuId = emuId;
      this.filename = filename;
    }
    int emuId;
    String filename;
  }

  @Override
  public void clearCache() {
    this.emulators.clear();
    this.gamesByEmu.clear();
    this.mapFilenames.clear();
  }

  /**
   * Get and reload all each time it is called, ie once at server startup
   */
  @Override
  public final List<Emulator> getEmulators() {
    // reset database
    clearCache();

    List<Emulator> loaded = loadEmulators();
    for (Emulator emu : loaded) {
      emulators.put(emu.getId(), emu);

      List<String> filenames = loadGames(emu);
      gamesByEmu.put(emu.getId(), filenames);

      for (String filename : filenames) {
        int id = filenameToId(emu.getId(), filename);
        GameEntry e = new GameEntry(emu.getId(), filename);
        mapFilenames.put(id, e);
      }
      LOG.info("Parsed games for emulator " + emu.getId() + ", " + emu.getName() + ": " + filenames.size() + " games");
    }
    return loaded;
  }

  protected int filenameToId(int emuId, String filename) {
    return (emuId + "@" + filename).hashCode() & Integer.MAX_VALUE;
  }
  private int findIdFromFilename(int emuId, String filename) {
    for (Map.Entry<Integer, GameEntry> entry: mapFilenames.entrySet()) {
      GameEntry e = entry.getValue();
      if (e.emuId == emuId && StringUtils.equalsIgnoreCase(e.filename, filename)) {
        return entry.getKey();
      }
    }
    return -1;
  }


  protected String getEmulatorExtension(String emuName) {
    String emu = emuName.replaceAll(" ", "").toLowerCase();
    switch (emu) {
      case "futurepinball":
        return "";
      case "visualpinball":
        return "vpx";
      case "zaccaria":
        return "";
      case "pinballfx2":
        return "fp2";
      case "pinballfx3":
        return "fp3";
      case "pinballarcade":
        return "";
      default:
        return null;
    }
  }

  /**
   * To be implemented by parent, to load a list of Emulators
   */
  protected abstract List<Emulator> loadEmulators();

  /**
   * To be implemented by parent, to list games for an Emulators
   */
  protected abstract List<String> loadGames(Emulator emu);

  /**
   * Get from the connector a game from DB
   */
  protected abstract TableDetails getGameFromDb(int emuId, String filename);

  /**
   * Notify the connector a game has been updated and the change should be reflected in DB
   */
  protected abstract void updateGameInDb(int emuId, String filename, TableDetails details);

  /**
   * To be implemented to drop the database
   */
  protected abstract void dropGameFromDb(int emuId, String filename);

  /**
   * To be implemented to persist modification in database
   */
  protected abstract void commitDb(Emulator emu);

  //-------------------------------------------------

  public Emulator getEmulator(int emulatorId) {
    return emulators.get(emulatorId);
  }


  @NonNull
  @Override
  public List<Game> getGames() {
    List<Game> games = new ArrayList<>();
    for (Integer emuId : emulators.keySet()) {
      List<Game> gamesForEmu = getGamesByEmulator(emuId);
      games.addAll(gamesForEmu);
    }
    return games;
  }

  @Override
  public Game getGame(int id) {
    GameEntry e = mapFilenames.get(id);
    return (e != null ? getGameByFilename(e.emuId, e.filename) : null);
  }

  @Override
  public Game getGameByFilename(int emuId, String filename) {

    if (gamesByEmu.get(emuId).contains(filename)) {

      Emulator emu = emulators.get(emuId);

      int id = findIdFromFilename(emuId, filename);

      TableDetails details = getGameFromDb(emuId, filename);
      String gameName = FilenameUtils.getBaseName(filename);

      Game game = new Game();
      game.setEmulatorId(emuId);
      game.setId(id);
      game.setGameName(details != null ? details.getGameName() : gameName);
      game.setGameFileName(details != null ? details.getGameFileName() : filename);
      game.setGameDisplayName(details != null ? details.getGameDisplayName() : gameName);
      game.setDisabled(details != null ? details.getStatus() == 0 : false);
      game.setVersion(details != null ? details.getGameVersion() : null);

      File table = new File(emu.getDirGames(), filename);
      game.setGameFile(table);

      game.setDateAdded(details != null ? details.getDateAdded() : null);
      game.setDateUpdated(details != null ? details.getDateModified() : null);
      return game;
    }
    return null;
  }

  @NonNull
  @Override
  public List<Game> getGamesByEmulator(int emuId) {
    List<String> filenames = gamesByEmu.get(emuId);
    List<Game> games = new ArrayList<>(filenames.size());
    for (String filename : filenames) {
      games.add(getGameByFilename(emuId, filename));
    }
    return games;
  }

  @NonNull
  @Override
  public List<Game> getGamesByFilename(String filename) {
    String gameName = filename.replaceAll("'", "''");
    return getGames().stream().filter(g -> StringUtils.containsIgnoreCase(g.getGameFileName(), gameName)).collect(Collectors.toList());
  }

  @Override
  public Game getGameByName(int emuId, String gameName) {
    return getGameByFilename(emuId, gameName);
    //return getGames().stream().filter(g -> g.getGameName()==gameName).findFirst().orElse(null);
  }

  @Override
  public int getGameCount(int emuId) {
    return gamesByEmu.get(emuId).size();
  }

  @Override
  public List<Integer> getGameIds(int emuId) {
    return gamesByEmu.get(emuId).stream().map(f -> findIdFromFilename(emuId, f)).collect(Collectors.toList());
  }

  //------------------------------------------------------------

  @Override
  public TableDetails getTableDetails(int id) {
    GameEntry e = mapFilenames.get(id);
    return (e != null ? getGameFromDb(e.emuId, e.filename): null);
  }

  @Override
  public void saveTableDetails(int id, TableDetails tableDetails) {
    GameEntry e = mapFilenames.get(id);
    if (e==null) {
      return;
    }

    // detection of file renamed
    if (!StringUtils.equalsIgnoreCase(e.filename, tableDetails.getGameFileName())) {
      deleteGame(id, false);

      // update filename, but do not change the id 
      e.filename = tableDetails.getGameFileName();
      mapFilenames.put(id, e);
      gamesByEmu.get(tableDetails.getEmulatorId()).add(e.filename);
    }
    updateGameInDb(e.emuId, e.filename, tableDetails);
    commitDb(emulators.get(tableDetails.getEmulatorId()));
  }

  @Override
  public void vpsLink(int gameId, String extTableId, String extTableVersionId) {
    //do nothing by default as this is stored in the internal database
  }


  //------------------------------------------------------------

  @Override
  public boolean deleteGame(int gameId) {
    return deleteGame(gameId, true);
  }

  private boolean deleteGame(int gameId, boolean commit) {
    GameEntry e = mapFilenames.remove(gameId);
    if (e != null) {
      List<String> games = gamesByEmu.get(e.emuId);
      games.remove(e.filename);

      dropGameFromDb(e.emuId, e.filename);
      if (commit) {
        commitDb(emulators.get(e.emuId));
      }
      return true;
    }
    return false;
  }

  @Override
  public void deleteGames(int emuId) {
    List<String> filenames = gamesByEmu.remove(emuId);
    for (String filename : filenames) {
      int id = findIdFromFilename(emuId, filename);      
      mapFilenames.remove(id);
      dropGameFromDb(emuId, filename);
    }
    gamesByEmu.put(emuId, new ArrayList<>());
    commitDb(emulators.get(emuId));
  }

  @Override
  public int importGame(int emulatorId,
                        @NonNull String gameName, @NonNull String gameFileName, @NonNull String gameDisplayName,
                        @Nullable String launchCustomVar, @NonNull java.util.Date dateFileUpdated) {
    LOG.info("Add game entry for '" + gameName + "', file name '" + gameFileName + "'");

    TableDetails details = new TableDetails();
    details.setEmulatorId(emulatorId);
    details.setGameName(gameName);
    details.setGameFileName(gameFileName);
    details.setGameDisplayName(gameDisplayName);
    details.setDateAdded(new java.util.Date());
    details.setLaunchCustomVar(launchCustomVar);
    details.setStatus(1); // enable game

    updateGameInDb(emulatorId, gameFileName, details);

    // if everything is good, the game id should have been generated, so add to the cached model
    int id = filenameToId(emulatorId, gameFileName);
    GameEntry e = new GameEntry(emulatorId, gameFileName);
    mapFilenames.put(id, e);
    gamesByEmu.get(emulatorId).add(gameFileName);

    // time to persist the file
    commitDb(emulators.get(emulatorId));

    return id;
  }

  //------------------------------------------------------------

  public int getVersion() {
    return -1;
  }

  //---------------------------

  @Override
  public JsonSettings getSettings() {
    return null;
  }

  @Override
  public void saveSettings(@NonNull Map<String, Object> data) {
  }


  @Override
  public void updateTableFileUpdated(int id) {
    //String stmt = "UPDATE Games SET DateFileUpdated=? WHERE GameID=?";
  }

  @Override
  public boolean isPupPackDisabled(@NonNull Game game) {
    return false;
  }

  @Override
  public void setPupPackEnabled(@NonNull Game game, boolean enable) {
  }

  //----------------------------

  @NonNull
  @Override
  public Playlist getPlayList(int id) {
    Playlist playlist = new Playlist();
    playlist.setId(id);
    playlist.setName("name");
    //playlist.setPlayListSQL("sql");
    //playlist.setMenuColor(rs.getInt("MenuColor"));
    //playlist.setSqlPlayList(sqlPlaylist);
    return playlist;
  }

  @Override
  public File getPlaylistMediaFolder(@NonNull Playlist playList, @NonNull VPinScreen screen) {
    return null;
  }

  @Override
  public List<Playlist> getPlayLists() {
    List<Playlist> result = new ArrayList<>();
    return result;
  }

  @Override
  public void setPlaylistColor(int playlistId, long color) {
  }

  @Override
  public void addToPlaylist(int playlistId, int gameId, int favMode) {
  }

  @Override
  public void updatePlaylistGame(int playlistId, int gameId, int favMode) {
  }

  @Override
  public void deleteFromPlaylists(int gameId) {
  }

  @Override
  public void deleteFromPlaylist(int playlistId, int gameId) {
  }

  @Override
  public Playlist getPlayListForGame(int gameId) {
    return null;
  }


  @NonNull
  @Override
  public List<Integer> getGameIdsFromPlaylists() {
    return new ArrayList<>();
  }

  //-------------------------

  @Override
  public java.util.Date getStartDate() {
    return null;
  }

  @NonNull
  @Override
  public List<TableAlxEntry> getAlxData() {
    List<TableAlxEntry> result = new ArrayList<>();
        /*
        TableAlxEntry e = new TableAlxEntry();
        e.setDisplayName(rs.getString("GameDisplay"));
        e.setGameId(rs.getInt("GameId"));
        e.setUniqueId(rs.getInt("UniqueId"));
        e.setLastPlayed(rs.getDate("LastPlayed"));
        e.setTimePlayedSecs(rs.getInt("TimePlayedSecs"));
        e.setNumberOfPlays(rs.getInt("NumberPlays"));
        result.add(e);
        */
    return result;
  }

  @Override
  public List<TableAlxEntry> getAlxData(int gameId) {
    List<TableAlxEntry> result = new ArrayList<>();
        /*
        TableAlxEntry e = new TableAlxEntry();
        e.setDisplayName(rs.getString("GameDisplay"));
        e.setGameId(rs.getInt("GameId"));
        e.setUniqueId(rs.getInt("UniqueId"));
        e.setLastPlayed(rs.getDate("LastPlayed"));
        e.setTimePlayedSecs(rs.getInt("TimePlayedSecs"));
        e.setNumberOfPlays(rs.getInt("NumberPlays"));
        result.add(e);
        */
    return result;
  }

  @Override
  public boolean updateNumberOfPlaysForGame(int gameId, long value) {
    return false;
  }

  @Override
  public boolean updateSecondsPlayedForGame(int gameId, long seconds) {
    return false;
  }

  //-------------------------

  @Override
  public FrontendControl getFunction(@NonNull String description) {
    FrontendControl f = null;
    return f;
  }

  @NonNull
  @Override
  public FrontendControls getControls() {
    FrontendControls controls = new FrontendControls();
    return controls;
  }

  @Override
  public FrontendControl getPinUPControlFor(VPinScreen screen) {
    return null;
  }

  @Override
  public String toString() {
    return "Frontend Connector \"" + this.getFrontend().getFrontendType().name() + "\"";
  }
}
