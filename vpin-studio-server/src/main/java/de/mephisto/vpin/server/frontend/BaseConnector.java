package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.playlists.Playlist;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
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
  private BidiMap<Integer, String> mapFilenames = new DualHashBidiMap<>();


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
        int id = _filenameToId(filename);
        mapFilenames.put(id, filename);
      }
      LOG.info("Parsed games for emulator " + emu.getId() + ", " + emu.getName() + ": " + filenames.size() + " games");
    }
    return loaded;
  }

  protected int _filenameToId(String filename) {
    return filename.hashCode() & Integer.MAX_VALUE;
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
  protected abstract TableDetails getGameFromDb(String filename);

  /**
   * Notify the connector a game has been updated and the change should be reflected in DB
   */
  protected abstract void updateGameInDb(String filename, TableDetails details);

  /**
   * To be implemented to drop the database
   */
  protected abstract void dropGameFromDb(String filename);

  /**
   * To be implemented to persist modification in database
   */
  protected abstract void commitDb(Emulator emu);

  //-------------------------------------------------

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
    String filename = mapFilenames.get(id);
    return filename != null ? getGameByFilename(filename) : null;
  }

  @Override
  public Game getGameByFilename(String filename) {
    for (Integer emuId : emulators.keySet()) {
      if (gamesByEmu.get(emuId).contains(filename)) {

        Emulator emu = emulators.get(emuId);

        int id = mapFilenames.getKey(filename);

        TableDetails details = getGameFromDb(filename);
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
    }
    return null;
  }

  @NonNull
  @Override
  public List<Game> getGamesByEmulator(int emulatorId) {
    List<String> filenames = gamesByEmu.get(emulatorId);
    List<Game> games = new ArrayList<>(filenames.size());
    for (String filename : filenames) {
      games.add(getGameByFilename(filename));
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
  public Game getGameByName(String gameName) {
    return getGameByFilename(gameName);
    //return getGames().stream().filter(g -> g.getGameName()==gameName).findFirst().orElse(null);
  }

  @Override
  public int getGameCount(int emuId) {
    return gamesByEmu.get(emuId).size();
  }

  @Override
  public List<Integer> getGameIds(int emuId) {
    return gamesByEmu.get(emuId).stream().map(f -> mapFilenames.getKey(f)).collect(Collectors.toList());
  }

  //------------------------------------------------------------

  @Override
  public TableDetails getTableDetails(int id) {
    String filename = mapFilenames.get(id);
    return getGameFromDb(filename);
  }

  @Override
  public void saveTableDetails(int id, TableDetails tableDetails) {
    String filename = mapFilenames.get(id);

    // detection of file renamed
    if (!StringUtils.equalsIgnoreCase(filename, tableDetails.getGameFileName())) {
      deleteGame(id, false);

      // update filename, but do not change the id 
      filename = tableDetails.getGameFileName();
      mapFilenames.put(id, filename);
      gamesByEmu.get(tableDetails.getEmulatorId()).add(filename);
    }
    updateGameInDb(filename, tableDetails);
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
    String filename = mapFilenames.remove(gameId);
    if (filename != null) {
      int _emuId = -1;
      for (Integer emuId : emulators.keySet()) {
        List<String> games = gamesByEmu.get(emuId);
        if (games.remove(filename)) {
          _emuId = emuId;
          break;
        }
      }
      if (_emuId >= 0) {
        dropGameFromDb(filename);
        if (commit) {
          commitDb(emulators.get(_emuId));
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public void deleteGames(int emulatorId) {
    List<String> filenames = gamesByEmu.remove(emulatorId);
    for (String filename : filenames) {
      mapFilenames.removeValue(filename);
      dropGameFromDb(filename);
    }
    gamesByEmu.put(emulatorId, new ArrayList<>());
    commitDb(emulators.get(emulatorId));
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

    updateGameInDb(gameFileName, details);

    // if everything is good, the game id should have been generated, so add to the cached model
    int id = _filenameToId(gameFileName);
    mapFilenames.put(id, gameFileName);
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
    return new java.util.Date();
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
