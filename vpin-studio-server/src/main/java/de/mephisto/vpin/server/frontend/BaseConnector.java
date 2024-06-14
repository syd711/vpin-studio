package de.mephisto.vpin.server.frontend;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.popper.Emulator;
import de.mephisto.vpin.restclient.popper.PinUPControl;
import de.mephisto.vpin.restclient.popper.PinUPControls;
import de.mephisto.vpin.restclient.popper.Playlist;
import de.mephisto.vpin.restclient.popper.PopperCustomOptions;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class BaseConnector implements FrontendConnector {

  private final static Logger LOG = LoggerFactory.getLogger(StandaloneConnector.class);

  /**
   * the loaded and cached emulators
   */
  protected Map<Integer, Emulator> emulators;
  /**
   * Map by emulator of ids
   */
  protected Map<Integer, List<String>> gamesByEmu;
  /**
   * Map id to filename
   */
  protected Map<Integer, String> mapFilenames;

  /**
   * Get and reload all each time it is called, ie once at server startup
   */
  @Override
  public final List<Emulator> getEmulators() {
    // reset database
    this.emulators = new HashMap<>();
    this.gamesByEmu = new HashMap<>();
    this.mapFilenames = new HashMap<>();

    List<Emulator> loaded = loadEmulators();
    for (Emulator emu : loaded) {
      emulators.put(emu.getId(), emu);

      List<String> filenames = loadGames(emu);
      gamesByEmu.put(emu.getId(), filenames);
      for (String filename : filenames) {
        int id = filenameToId(filename);
        mapFilenames.put(id, filename);
      }
      LOG.info("Parsed games for emulator " + emu.getId() + ", " + emu.getName() + ": " + filenames.size() + " games");
    }
    return loaded;
  }

  protected int filenameToId(String filename) {
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
  protected abstract void commitDb();

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

        File tableFile = new File(filename);
        String tableName = StringUtils.removeEndIgnoreCase(tableFile.getName(), "." + emu.getGamesExt());

        Game game = new Game();
        game.setEmulatorId(emuId);
        game.setId(filenameToId(filename));
        game.setGameName(tableName);
        game.setGameFileName(filename);
        game.setGameDisplayName(tableName);
        game.setDisabled(false);

        File table = new File(emu.getDirGames(), filename);
        game.setGameFile(table);

        game.setDateUpdated(new java.util.Date(table.lastModified()));
        return game;
      }
    }
    return null;
  }

  @Override
  public List<Game> getGamesByEmulator(int emulatorId) {
    List<String> filenames = gamesByEmu.get(emulatorId);
    List<Game> games = new ArrayList<>(filenames.size());
    for (String filename : filenames) {
      games.add(getGameByFilename(filename));
    }
    return games;
  }

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
    return gamesByEmu.get(emuId).stream().map(f -> filenameToId(f)).collect(Collectors.toList());
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
    updateGameInDb(filename, tableDetails);
    commitDb();
  }

  protected TableDetails fromGame(Game game) {
    TableDetails manifest = new TableDetails();

    manifest.setEmulatorId(game.getEmulatorId());
    manifest.setGameName(game.getGameName());
    manifest.setGameFileName(game.getGameFileName());
    manifest.setGameDisplayName(game.getGameDisplayName());
    manifest.setGameVersion(game.getVersion());
    manifest.setRomName(game.getRom());
    manifest.setRomAlt(game.getTableName());

    manifest.setDateAdded(game.getDateAdded());
    // cf statuses: STATUS_DISABLED=0, STATUS_NORMAL=1, STATUS_MATURE=2, STATUS_WIP=3
    manifest.setStatus(game.isDisabled() ? 0 : 1);
    manifest.setWebGameId(game.getExtTableId());

    return manifest;
  }

  //------------------------------------------------------------

  @Override
  public boolean deleteGame(int gameId) {
    String filename = mapFilenames.remove(gameId);
    if (filename != null) {
      for (Integer emuId : emulators.keySet()) {
        List<String> games = gamesByEmu.get(emuId);
        games.remove(filename);
      }
      dropGameFromDb(filename);
      commitDb();
      return true;
    }
    return false;
  }

  public void deleteGames() {
    mapFilenames.clear();
    for (Integer emuId : emulators.keySet()) {
      gamesByEmu.get(emuId).clear();
    }
    dropGameFromDb(null);
    commitDb();
  }

  @Override
  public int importGame(int emulatorId,
                        @NonNull String gameName, @NonNull  String gameFileName, @NonNull  String gameDisplayName,
                        @Nullable String launchCustomVar, @NonNull java.util.Date dateFileUpdated) {
    LOG.info("Add game entry for '" + gameName + "', file name '" + gameFileName + "'");

    TableDetails details = new TableDetails();
    details.setEmulatorId(emulatorId);
    details.setGameName(gameName);
    details.setGameFileName(gameFileName);
    details.setGameDisplayName(gameDisplayName);
    details.setDateAdded(new java.util.Date());
    details.setLaunchCustomVar(launchCustomVar);

    updateGameInDb(gameFileName, details);
    commitDb();

    // if everything is good, the game id should have been generated, so add to the cached model
    int id = filenameToId(gameFileName);
    mapFilenames.put(id, gameFileName);
    gamesByEmu.get(emulatorId).add(gameFileName);
    return id;
  }

  //------------------------------------------------------------

  public int getVersion() {
    return -1;
  }

  @Override
  public boolean isPopper15() {
    return true;
  }

  //---------------------------

  @Override
  public PopperCustomOptions getCustomOptions() {
    return new PopperCustomOptions();
  }

  @Override
  public void updateCustomOptions(@NonNull PopperCustomOptions options) {
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
  public List<Playlist> getPlayLists(boolean excludeSqlLists) {
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
  public PinUPControl getFunction(@NonNull String description) {
    PinUPControl f = null;
    return f;
  }

  @NonNull
  @Override
  public PinUPControls getControls() {
    PinUPControls controls = new PinUPControls();
    return controls;
  }

  @Override
  public PinUPControl getPinUPControlFor(PopperScreen screen) {
    return null;
  }

}
