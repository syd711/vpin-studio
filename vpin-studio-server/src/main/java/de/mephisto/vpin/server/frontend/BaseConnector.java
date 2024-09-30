package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.frontend.*;

import de.mephisto.vpin.server.games.*;
import de.mephisto.vpin.server.playlists.Playlist;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

  /**
   * set of favorite gameId
   */
  private Set<Integer> gameFavs = new HashSet<>();

  /**
   * A cache of Playlists indexed by their id
   */
  private Map<Integer, Playlist> playlists = new HashMap<>();

    /**
   * map between gameId and stat
   */
  private Map<Integer, TableAlxEntry> gameStats = new HashMap<>();


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
    this.playlists.clear();
    this.gameFavs.clear();
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

    // force initialisation of cache
    getAlxData();

    // load and cache playlists
    List<Playlist> loadedPlaylists = loadPlayLists();
    if (loadedPlaylists != null) {
      for (Playlist playlist : loadedPlaylists) {
        playlists.put(playlist.getId(), playlist);
        // get color if set
        Map<String, ?> playlistConf = getPlaylistConf(playlist);
        playlist.setMenuColor((Integer) playlistConf.get("menuColor"));
      }
    }

    // load and cache favorites
    gameFavs = loadFavorites();

    return loaded;
  }

  public int filenameToId(int emuId, String filename) {
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

  public String getGameFilename(int id) {
    GameEntry e = mapFilenames.get(id);
    return (e != null ? e.filename : null);
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
      //game.setMediaStrategy(getMediaAccessStrategy());
      game.setEmulatorId(emuId);
      game.setId(id);
      game.setGameName(details != null ? details.getGameName() : gameName);
      game.setGameFileName(details != null ? details.getGameFileName() : filename);
      game.setGameDisplayName(details != null ? details.getGameDisplayName() : gameName);
      game.setGameStatus(details != null ? details.getStatus(): 1);
      game.setDisabled(details != null ? details.getStatus() == 0 : false);
      game.setVersion(details != null ? details.getGameVersion() : null);

      File table = new File(emu.getDirGames(), filename);
      game.setGameFile(table);

      game.setDateAdded(details != null ? details.getDateAdded() : null);
      game.setDateUpdated(details != null ? details.getDateModified() : null);

      TableAlxEntry stat = getGameStat(id);
      if (stat != null) {
        game.setNumberPlayed(stat.getNumberOfPlays());
      }

      return game;
    }
    return null;
  }

  @NonNull
  @Override
  public List<Game> getGamesByEmulator(int emuId) {
    List<String> filenames = gamesByEmu.get(emuId);
    List<Game> games = new ArrayList<>();
    if (filenames != null) {
      for (String filename : filenames) {
        games.add(getGameByFilename(emuId, filename));
      }
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
  }

  @Override
  public int getGameCount(int emuId) {
    return gamesByEmu.get(emuId).size();
  }

  public List<String> getGameFilenames(int emuId) {
    return gamesByEmu.get(emuId);
  }

  @Override
  public List<Integer> getGameIds(int emuId) {
    return getGameFilenames(emuId).stream().map(f -> findIdFromFilename(emuId, f)).collect(Collectors.toList());
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
  public void setPupPackEnabled(@NonNull Game game, boolean enable) {
  }

  //----------------------------

  protected List<Playlist> loadPlayLists() {
    return null;
  }

  protected void savePlaylist(Playlist playlist) {
  }

  public Set<Integer> loadFavorites() {
    return new HashSet<>();
  }

  protected void saveFavorite(int gameId, boolean favorite) {
  }

  private Playlist getFavPlaylist() {
    Playlist favs = new Playlist();
    favs.setId(-1);
    favs.setName("Favorites");
    List<PlaylistGame> favspg = gameFavs.stream().map(id -> toPlaylistGame(id)).collect(Collectors.toList());
    favs.setGames(favspg);
    return favs;
  }
 
  @NonNull
  @Override
  public Playlist getPlayList(int id) {
    return id == -1 ? getFavPlaylist() : playlists.get(id);
  }

  @Override
  public List<Playlist> getPlayLists() {
    List<Playlist> result = new ArrayList<>();

    Playlist favs = getFavPlaylist();
    result.add(favs);

    for (Map.Entry<Integer, Playlist> playlist : playlists.entrySet()) {
      result.add(playlist.getValue());
    }

    return result;
  }

  protected PlaylistGame toPlaylistGame(int gameId) {
    PlaylistGame pg = new PlaylistGame();
    pg.setId(gameId);

    TableAlxEntry gamestat = gameStats.get(gameId);
    if (gamestat != null) {
      pg.setPlayed(gamestat.getNumberOfPlays() > 0);
      pg.setFav(gameFavs.contains(gameId));
      pg.setGlobalFav(false);
    }
    else {
      pg.setPlayed(false);
    }

    return pg;
  }

  @Override
  public File getPlaylistMediaFolder(@NonNull Playlist playList, @NonNull VPinScreen screen) {
    File pinballXFolder = getInstallationFolder();
    // not standard but why not...
    File mediaDir = new File(pinballXFolder, "Media/Playlists");
    return new File(mediaDir, screen.getSegment());
  }

  @Override
  public void addToPlaylist(int playlistId, int gameId, int favMode) {
    if (playlistId >= 0) {
      Playlist pl = playlists.get(playlistId);
      if (!pl.containsGame(gameId)) {
        pl.getGames().add(toPlaylistGame(gameId));
      }
      savePlaylist(pl);
    }
    else {
      gameFavs.add(gameId);
      saveFavorite(gameId, true);
    }
  }

  @Override
  public void updatePlaylistGame(int playlistId, int gameId, int favMode) {
    // not used
  }

  @Override
  public void deleteFromPlaylists(int gameId) {
    for (Integer playlistId : playlists.keySet()) {
      deleteFromPlaylist(playlistId, gameId);
    }
  }

  @Override
  public void deleteFromPlaylist(int playlistId, int gameId) {
    if (playlistId >= 0) {
      Playlist pl = playlists.get(playlistId);
      if (pl.removeGame(gameId)) {
        savePlaylist(pl);
      }
    }
    else {
      if (gameFavs.remove(gameId)) {
        saveFavorite(gameId, false);
      }
    }
  }

  @Override
  public void setPlaylistColor(int playlistId, long color) {
    Playlist playlist = getPlayList(playlistId);
    if (playlist != null) {
      playlist.setMenuColor((int) color);
      Map<String, Object> playlistConf = getPlaylistConf(playlist);
      playlistConf.put("menuColor", color);
      savePlaylistConf(playlist, playlistConf);
    }
  }

  private File getPlaylistConfFile() {
    File pinballXFolder = getInstallationFolder();
    return new File(pinballXFolder, "/Databases/playlists.json");
  }

  private Map<String, Object> getPlaylistConf(Playlist playlist) {
    File playlistConfFile = getPlaylistConfFile();
    if (playlistConfFile != null && playlistConfFile.exists()) {
      try {
        String content = Files.readString(playlistConfFile.toPath(), Charset.forName("UTF-8"));
        // convert JSON string to Map
        Map<String, Object>[] confs = new ObjectMapper().readValue(content, new TypeReference<>() {
        });
        for (Map<String, Object> conf : confs) {
          if (playlist.getName().equals(conf.get("name"))) {
            return conf;
          }
        }
      }
      catch (IOException ioe) {
        LOG.error("Ignored error, cannot read file " + playlistConfFile.getAbsolutePath(), ioe);
      }
    }
    return new HashMap<>();
  }

  private void savePlaylistConf(Playlist playlist, Map<String, ?> playlistConf) {
    File playlistConfFile = getPlaylistConfFile();
    if (playlistConfFile != null) {
      try {
        String content = new ObjectMapper().writeValueAsString(playlistConf);
        Files.write(playlistConfFile.toPath(), content.getBytes(Charset.forName("UTF-8")));
      }
      catch (IOException ioe) {
        LOG.error("Ignored error, cannot write file " + playlistConfFile.getAbsolutePath(), ioe);
      }
    }
  }

  //------------------------- STATISTICS --------------

  /**
   * To be implemented by parent to complete load
   */
  protected List<TableAlxEntry> loadStats() {
    return null;
  }

  protected TableAlxEntry getGameStat(int gameId) {
    return gameStats.get(gameId);
  }

  @Override
  public java.util.Date getStartDate() {
    return null;
  }

  @Override
  public final List<TableAlxEntry> getAlxData() {
    List<TableAlxEntry> stats = loadStats();
    if (stats != null) {
      // refresh cache of stats
      gameStats = new HashMap<>();
      for (TableAlxEntry stat : stats) {
        gameStats.put(stat.getGameId(), stat);
      }
      return stats;
    }
    return Collections.emptyList();
  }

  @Override
  public final List<TableAlxEntry> getAlxData(int gameId) {
    List<TableAlxEntry> result = new ArrayList<>();
    TableAlxEntry stat = getGameStat(gameId);
    if (stat != null) {
      result.add(stat);
    }
    return result;
  }

  @Override
  public boolean updateNumberOfPlaysForGame(int gameId, long value) {
    // update internal cache
    TableAlxEntry stat = gameStats.get(gameId);
    stat.setNumberOfPlays((int) value); 
    return true;
  }

  @Override
  public boolean updateSecondsPlayedForGame(int gameId, long seconds) {
    // update internal cache
    TableAlxEntry stat = gameStats.get(gameId);
    stat.setTimePlayedSecs((int) seconds); 
    return true;
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
