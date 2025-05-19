package de.mephisto.vpin.server.frontend;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.mephisto.vpin.commons.utils.NirCmd;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.server.fp.FPService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.playlists.Playlist;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpx.VPXService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseConnector implements FrontendConnector {
  private final static Logger LOG = LoggerFactory.getLogger(BaseConnector.class);

  @Autowired
  private VPXService vpxService;

  @Autowired
  private FPService fpService;

  @Autowired
  protected GameEntryRepository gameEntryRepository;

  @Autowired
  protected PreferencesService preferencesService;

  @Autowired
  protected SystemService systemService;

  /**
   * the loaded and cached emulators
   */
  protected Map<Integer, GameEmulator> emulators = new HashMap<>();
  /**
   * Map by emulator of GameEntry
   */
  protected Map<Integer, List<GameEntry>> gamesByEmu = new HashMap<>();
  /**
   * Map id <-> GameEntry
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

  private MediaAccessStrategy mediaAccessStrategy;

  private TableAssetsAdapter tableAssetsAdapter;


  /**
   * reload all each time it is called, ie once at server startup
   */
  @Override
  public void reloadCache() {
    this.emulators.clear();
    this.gamesByEmu.clear();
    this.mapFilenames.clear();
    this.playlists.clear();
    this.gameFavs.clear();

    // load all existing entries from database
    List<GameEntry> entries = gameEntryRepository.findAll();

    List<GameEmulator> loaded = loadEmulators();
    for (GameEmulator emu : loaded) {
      emulators.put(emu.getId(), emu);

      List<String> filenames = loadGames(emu);

      List<GameEntry> games = new ArrayList<>(filenames.size());
      for (String filename : filenames) {
        GameEntry e = popGameEntry(entries, emu.getId(), filename);
        games.add(e);
        mapFilenames.put(e.getId(), e);
      }
      gamesByEmu.put(emu.getId(), games);

      LOG.info("Parsed games for emulator " + emu.getId() + ", " + emu.getName() + ": " + filenames.size() + " games");
    }

    // remaining entries in the List are orphaned, delete them
    entries.forEach(e -> gameEntryRepository.delete(e));

    // force initialisation of cache for statistics
    getAlxData();

    // load and cache playlists
    playlists.clear();
    List<Playlist> loadedPlaylists = loadPlayLists();
    if (loadedPlaylists != null) {
      for (Playlist playlist : loadedPlaylists) {
        playlists.put(playlist.getId(), playlist);
        // get color if set
        JsonObject playlistConf = getPlaylistConf(playlist);
        if (playlistConf.has("menuColor")) {
          JsonElement menuColor = playlistConf.get("menuColor");
          if (!menuColor.isJsonNull()) {
            playlist.setMenuColor(menuColor.getAsInt());
          }
        }
      }
    }

    // load and cache favorites
    gameFavs = loadFavorites();
  }

  private GameEntry popGameEntry(List<GameEntry> entries, int emuId, String filename) {
    GameEntry entry = entries.stream()
        .filter(e -> e.getEmuId() == emuId && StringUtils.equalsIgnoreCase(e.getFilename(), filename))
        .findFirst().orElse(null);

    // new discovered entry, create id
    if (entry == null) {
      int id = filenameToId(emuId, filename);
      entry = new GameEntry(emuId, filename, id);
      gameEntryRepository.save(entry);
    }
    else {
      entries.remove(entry);
    }
    return entry;
  }

  private int filenameToId(int emuId, String filename) {
    return (emuId + "@" + filename).hashCode() & Integer.MAX_VALUE;
  }

  protected GameEntry findEntryFromFilename(int emuId, String filename) {
    for (Map.Entry<Integer, GameEntry> entry : mapFilenames.entrySet()) {
      GameEntry e = entry.getValue();
      if (e.getEmuId() == emuId && StringUtils.equalsIgnoreCase(e.getFilename(), filename)) {
        return e;
      }
    }
    return null;
  }

  protected int findIdFromFilename(int emuId, String filename) {
    GameEntry entry = findEntryFromFilename(emuId, filename);
    return entry != null ? entry.getId() : -1;
  }


  /**
   * To be implemented by parent, to load a list of Emulators
   */
  protected abstract List<GameEmulator> loadEmulators();

  /**
   * To be implemented by parent, to list games for an Emulators
   */
  protected abstract List<String> loadGames(GameEmulator emu);

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
  protected abstract void commitDb(GameEmulator emu);

  //-------------------------------------------------

  @Override
  public final List<GameEmulator> getEmulators() {
    return new ArrayList<>(emulators.values());
  }

  @Override
  public GameEmulator getEmulator(int emulatorId) {
    return emulators.get(emulatorId);
  }

  private Game getGame(GameEntry e) {
    if (e == null) {
      return null;
    }

    String filename = e.getFilename();
    GameEmulator emu = emulators.get(e.getEmuId());

    TableDetails details = getGameFromDb(e.getEmuId(), filename);
    String gameName = FilenameUtils.getBaseName(filename);

    Game game = new Game();
    game.setEmulatorId(e.getEmuId());
    game.setId(e.getId());
    game.setGameName(details != null ? details.getGameName() : gameName);
    game.setGameFileName(details != null ? details.getGameFileName() : filename);
    game.setGameDisplayName(details != null ? details.getGameDisplayName() : gameName);
    game.setGameStatus(details != null ? details.getStatus() : 1);
    game.setDisabled(details != null ? details.getStatus() == 0 : false);
    game.setVersion(details != null ? details.getGameVersion() : null);
    game.setRating(details != null && details.getGameRating() != null ? details.getGameRating() : 0);

    File table = new File(emu.getGamesDirectory(), filename);
    game.setGameFile(table);

    game.setDateAdded(details != null ? details.getDateAdded() : null);
    game.setDateUpdated(details != null ? details.getDateModified() : null);

    TableAlxEntry stat = getGameStat(e.getId());
    if (stat != null) {
      game.setNumberPlayed(stat.getNumberOfPlays());
    }

    return game;
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

  public GameEntry getGameEntry(int id) {
    return mapFilenames.get(id);
  }

  public String getGameFilename(int id) {
    GameEntry e = mapFilenames.get(id);
    return (e != null ? e.getFilename() : null);
  }

  @Override
  public Game getGame(int id) {
    GameEntry entry = mapFilenames.get(id);
    return getGame(entry);
  }

  @Override
  public Game getGameByFilename(int emuId, String filename) {
    GameEntry entry = findEntryFromFilename(emuId, filename);
    return getGame(entry);
  }

  @NonNull
  @Override
  public List<Game> getGamesByEmulator(int emuId) {
    List<GameEntry> entries = gamesByEmu.get(emuId);
    if (entries != null) {
      return entries.stream().map(e -> getGame(e)).collect(Collectors.toList());
    }
    return new ArrayList<>();
  }

  @NonNull
  @Override
  public List<Game> getGamesByFilename(String filename) {
    String gameFileName = filename.replaceAll("'", "''");
    return getGames().stream().filter(g -> StringUtils.containsIgnoreCase(g.getGameFileName(), gameFileName)).collect(Collectors.toList());
  }

  @Override
  public Game getGameByName(int emuId, String gameName) {
    return getGameEntries(emuId).stream()
        .map(e -> getGame(e))
        .filter(g -> StringUtils.containsIgnoreCase(g.getGameName(), gameName))
        .findFirst().orElse(null);
  }

  @Override
  public int getGameCount(int emuId) {
    return gamesByEmu.get(emuId).size();
  }

  public List<GameEntry> getGameEntries(int emuId) {
    return gamesByEmu.get(emuId);
  }

  @Override
  public List<Integer> getGameIds(int emuId) {
    return getGameEntries(emuId).stream().map(e -> e.getId()).collect(Collectors.toList());
  }

  //------------------------------------------------------------

  @Override
  public TableDetails getTableDetails(int id) {
    GameEntry e = mapFilenames.get(id);
    return (e != null ? getGameFromDb(e.getEmuId(), e.getFilename()) : null);
  }

  @Override
  public void saveTableDetails(int id, TableDetails tableDetails) {
    GameEntry e = mapFilenames.get(id);
    if (e == null) {
      return;
    }

    // detection of file renamed
    if (!StringUtils.equalsIgnoreCase(e.getFilename(), tableDetails.getGameFileName())) {
      deleteGame(id, false);

      // update filename, but do not change the id 
      e.setFilename(tableDetails.getGameFileName());
      mapFilenames.put(id, e);
      gamesByEmu.get(tableDetails.getEmulatorId()).add(e);
    }
    updateGameInDb(e.getEmuId(), e.getFilename(), tableDetails);
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
      List<GameEntry> games = getGameEntries(e.getEmuId());
      games.remove(e);

      dropGameFromDb(e.getEmuId(), e.getFilename());
      if (commit) {
        commitDb(emulators.get(e.getEmuId()));
      }
      return true;
    }
    return false;
  }

  @Override
  public void deleteGames(int emuId) {
    List<GameEntry> entries = gamesByEmu.remove(emuId);
    if (entries != null) {
      for (GameEntry entry : entries) {
        mapFilenames.remove(entry.getId());
        dropGameFromDb(emuId, entry.getFilename());
      }
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

    int id = filenameToId(emulatorId, gameFileName);
    GameEntry e = new GameEntry(emulatorId, gameFileName, id);
    gameEntryRepository.save(e);

    mapFilenames.put(id, e);
    gamesByEmu.get(emulatorId).add(e);

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

  @Override
  public void savePlaylistOrder(PlaylistOrder playlistOrder) {
    //not used
  }

  @Override
  public Playlist savePlaylist(Playlist playlist) {
    if (playlist.getId() < -1) {
      UISettings uiSettings = preferencesService.getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      if (playlist.getId() == PlaylistRepresentation.PLAYLIST_MOSTPLAYED_ID) {
        uiSettings.setMostPlayedColor(WidgetFactory.hexColor(playlist.getMenuColor()));
      }
      else if (playlist.getId() == PlaylistRepresentation.PLAYLIST_FAVORITE_ID) {
        uiSettings.setLocalFavsColor(WidgetFactory.hexColor(playlist.getMenuColor()));
      }
      else if (playlist.getId() == PlaylistRepresentation.PLAYLIST_GLOBALFAV_ID) {
        uiSettings.setGlobalFavsColor(WidgetFactory.hexColor(playlist.getMenuColor()));
      }
      else if (playlist.getId() == PlaylistRepresentation.PLAYLIST_JUSTADDED_ID) {
        uiSettings.setJustAddedColor(WidgetFactory.hexColor(playlist.getMenuColor()));
      }
      try {
        preferencesService.savePreference(PreferenceNames.UI_SETTINGS, uiSettings);
      }
      catch (Exception e) {
        LOG.error("Failed to save playlist colors: {}", e.getMessage(), e);
      }
    }
    else {
      if (playlist.getId() == -1) {
        // newly created playlist, generate a dummy id and add to memory
        int newid = Collections.max(playlists.keySet()) + 1;
        playlist.setId(newid);
      }
      // update cache
      playlists.put(playlist.getId(), playlist);

      savePlaylistConf(playlist);
    }
    return playlist;
  }

  @Override
  public boolean deletePlaylist(int playlistId) {
    Playlist playlist = playlists.remove(playlistId);
    if (playlist == null) {
      return false;
    }
    deletePlaylistConf(playlist);
    return true;
  }

  protected void savePlaylistGame(int gameId, Playlist playlist) {

  }

  public Set<Integer> loadFavorites() {
    return new HashSet<>();
  }

  protected void saveFavorite(int gameId, boolean favorite) {
  }

  private Playlist getFavPlaylist(UISettings uiSettings) {
    Playlist favs = new Playlist();
    favs.setId(PlaylistRepresentation.PLAYLIST_FAVORITE_ID);
    favs.setName("Favorites");
    int intValue = toColorCode(uiSettings.getLocalFavsColor());
    favs.setMenuColor(intValue);
    List<PlaylistGame> favspg = gameFavs.stream().map(id -> toPlaylistGame(id)).collect(Collectors.toList());
    favs.setGames(favspg);
    return favs;
  }

  private int toColorCode(String color) {
    if (color.startsWith("#")) {
      color = color.substring(1);
    }
    return Integer.valueOf(color, 16);
  }

  private Playlist getJustAddedPlaylist(UISettings uiSettings) {
    Playlist pl = new Playlist();
    pl.setId(PlaylistRepresentation.PLAYLIST_JUSTADDED_ID);
    pl.setName("Just Added");
    int intValue = toColorCode(uiSettings.getJustAddedColor());
    pl.setMenuColor(intValue);
    pl.setSqlPlayList(true);
    long dayMinus7 = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000;
    List<PlaylistGame> games = getGames().stream().filter(g -> {
      return g.getDateAdded() != null ? g.getDateAdded().getTime() > dayMinus7 : false;
    }).map(g -> toPlaylistGame(g.getId())).collect(Collectors.toList());
    pl.setGames(games);
    return pl;
  }

  private Playlist getMostPlayedPlaylist(UISettings uiSettings) {
    Playlist pl = new Playlist();
    pl.setId(PlaylistRepresentation.PLAYLIST_MOSTPLAYED_ID);
    pl.setName("Most Played");
    int intValue = toColorCode(uiSettings.getMostPlayedColor());
    pl.setMenuColor(intValue);
    pl.setSqlPlayList(true);

    // extract stats, sort by number of plays, take first 10 and return PLaylistGames
    Comparator<TableAlxEntry> c = (s1, s2) -> s1.getNumberOfPlays() == s2.getNumberOfPlays() ?
        s2.getTimePlayedSecs() - s1.getTimePlayedSecs() :
        s2.getNumberOfPlays() - s1.getNumberOfPlays();

    List<TableAlxEntry> games = getAlxData().stream().sorted(c).limit(10)
        .collect(Collectors.toList());

    List<PlaylistGame> plgames = games.stream().map(s -> toPlaylistGame(s.getGameId()))
        .collect(Collectors.toList());

    pl.setGames(plgames);
    return pl;
  }

  @NonNull
  @Override
  public Playlist getPlaylist(int id) {
    UISettings uiSettings = preferencesService.getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    return id == PlaylistRepresentation.PLAYLIST_FAVORITE_ID ? getFavPlaylist(uiSettings) :
        id == PlaylistRepresentation.PLAYLIST_JUSTADDED_ID ? getJustAddedPlaylist(uiSettings) :
            id == PlaylistRepresentation.PLAYLIST_MOSTPLAYED_ID ? getMostPlayedPlaylist(uiSettings) :
                playlists.get(id);
  }

  @NonNull
  @Override
  public Playlist clearPlaylist(int id) {
    Playlist playlist = getPlaylist(id);
    List<PlaylistGame> games = playlist.getGames();
    for (PlaylistGame game : games) {
      deleteFromPlaylist(id, game.getId());
    }
    return getPlaylist(id);
  }

  @NonNull
  @Override
  public Playlist getPlaylistTree() {
    Playlist artificialRoot = new Playlist();
    artificialRoot.setId(0);
    artificialRoot.setName("--");
    artificialRoot.setChildren(getPlaylists());
    return artificialRoot;
  }

  @Override
  public List<Playlist> getPlaylists() {
    List<Playlist> result = new ArrayList<>();

    UISettings uiSettings = preferencesService.getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    result.add(getFavPlaylist(uiSettings));
    result.add(getJustAddedPlaylist(uiSettings));
    result.add(getMostPlayedPlaylist(uiSettings));

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
  public void addToPlaylist(int playlistId, int gameId, int favMode) {
    if (playlistId >= 0) {
      Playlist pl = playlists.get(playlistId);
      if (!pl.containsGame(gameId)) {
        pl.getGames().add(toPlaylistGame(gameId));
      }
      savePlaylistGame(gameId, pl);
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
        savePlaylistGame(gameId, pl);
      }
    }
    else {
      if (gameFavs.remove(gameId)) {
        saveFavorite(gameId, false);
      }
    }
  }

  private File getPlaylistConfFile() {
    File installFolder = getInstallationFolder();
    return new File(installFolder, "/Databases/playlists.json");
  }

  private JsonObject getPlaylistConf() {
    File playlistConfFile = getPlaylistConfFile();
    if (playlistConfFile != null && playlistConfFile.exists()) {
      try {
        String content = Files.readString(playlistConfFile.toPath(), Charset.forName("UTF-8"));
        JsonElement e = JsonParser.parseString(content);
        if (e.isJsonObject()) {
          return (JsonObject) e;
        }
      }
      catch (IOException ioe) {
        LOG.error("Ignored error, cannot read file " + playlistConfFile.getAbsolutePath(), ioe);
      }
    }
    return new JsonObject();
  }

  protected JsonObject getPlaylistConf(Playlist playlist) {
    JsonObject conf = getPlaylistConf();
    JsonObject playlistConf = conf.getAsJsonObject(playlist.getName());
    if (playlistConf == null) {
      return new JsonObject();
    }
    return playlistConf;
  }

  protected void savePlaylistConf(Playlist playlist) {
    JsonObject o = getPlaylistConf();
    JsonObject playlistConf = getPlaylistConf(playlist);
    o.add(playlist.getName(), playlistConf);
    playlistConf.addProperty("menuColor", playlist.getMenuColor());
    File playlistConfFile = getPlaylistConfFile();
    if (playlistConfFile != null) {
      try {
        String content = o.toString();
        Files.write(playlistConfFile.toPath(), content.getBytes(StandardCharsets.UTF_8));
      }
      catch (IOException ioe) {
        LOG.error("Ignored error, cannot write file " + playlistConfFile.getAbsolutePath(), ioe);
      }
    }
  }

  protected void deletePlaylistConf(Playlist playlist) {
    JsonObject o = getPlaylistConf();
    o.remove(playlist.getName());
    File playlistConfFile = getPlaylistConfFile();
    if (playlistConfFile != null) {
      try {
        String content = o.toString();
        Files.write(playlistConfFile.toPath(), content.getBytes(StandardCharsets.UTF_8));
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
  public MediaAccessStrategy getMediaAccessStrategy() {
    return mediaAccessStrategy;
  }

  public void setMediaAccessStrategy(MediaAccessStrategy mediaAccessStrategy) {
    this.mediaAccessStrategy = mediaAccessStrategy;
  }

  @Override
  public TableAssetsAdapter getTableAssetAdapter() {
    return tableAssetsAdapter;
  }

  public void setTableAssetAdapter(TableAssetsAdapter tableAssetsAdapter) {
    this.tableAssetsAdapter = tableAssetsAdapter;
  }

  @Override
  public FrontendControl getFrontendControl(@NonNull String description) {
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
  public FrontendControl getFrontendControlFor(VPinScreen screen) {
    return null;
  }

  @Override
  public String toString() {
    return getClass().getName();
  }

  //----------------------------
  // UI Management

  protected abstract String getFrontendExe();

  protected File resolveExe(EmulatorType type) {
    switch (type) {
      case VisualPinball:
        return systemService.resolveVpx64Exe();
      case VisualPinball9:
        return systemService.resolveVptExe();
      case FuturePinball:
        return systemService.resolveFpExe();
      default:
        return null;
    }
  }

  @Override
  public boolean killFrontend() {
    NirCmd.setTaskBarVisible(true);
    return killEmulators(true);
  }

  private boolean killEmulators(boolean withFrontend) {
    List<ProcessHandle> processes = ProcessHandle
        .allProcesses()
        .filter(p -> p.info().command().isPresent() &&
            (
                withFrontend && p.info().command().get().contains(getFrontendExe()) ||
                    p.info().command().get().contains("PinUpDisplay") ||
                    p.info().command().get().contains("PinUpPlayer") ||
                    p.info().command().get().contains("VPXStarter") ||
                    p.info().command().get().contains("VPinballX") ||
                    p.info().command().get().contains("Future Pinball") ||
                    p.info().command().get().startsWith("VPinball") ||
                    p.info().command().get().contains("B2SBackglassServerEXE") ||
                    p.info().command().get().contains("DOF")))
        .collect(Collectors.toList());

    if (processes.isEmpty()) {
      LOG.info("No vpin processes found, termination canceled.");
      return false;
    }

    for (ProcessHandle process : processes) {
      String cmd = process.info().command().get();
      boolean b = process.destroyForcibly();
      LOG.info("Destroyed process '" + cmd + "', result: " + b);
    }
    return true;
  }

  @Override
  public boolean isFrontendRunning() {
    Optional<ProcessHandle> process = ProcessHandle
        .allProcesses()
        .filter(p -> p.info().command().isPresent() &&
            p.info().command().get().contains(getFrontendExe()))
        .findFirst();
    return process.isPresent();
  }

  @Override
  public boolean restartFrontend() {
    killFrontend();

    String exe = getFrontendExe();
    try {
      List<String> params = Arrays.asList("cmd", "/c", "start", exe);
      SystemCommandExecutor executor = new SystemCommandExecutor(params, false);
      executor.setDir(getInstallationFolder());
      executor.executeCommandAsync();

      //StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error(exe + " restart failed: {}", standardErrorFromCommand);
        return false;
      }
    }
    catch (Exception e) {
      LOG.error("Failed to start " + exe + " again: " + e.getMessage(), e);
      return false;
    }
    return true;
  }

  @Override
  public boolean launchGame(Game game) {
    return launchGame(game, false);
  }

  private boolean launchGame(Game game, boolean wait) {
    GameEmulator emu = game.getEmulator();
    if (emu.isVpxEmulator()) {
      if (vpxService.play(game, null, null)) {
        return !wait ? true : vpxService.waitForPlayer();
      }
      return false;
    }
    else if (emu.isFpEmulator()) {
      return fpService.play(game, null);
    }
    else {
      LOG.error("Emulator {} for Game \"{}\" cannot be started", emu.getName(), game.getGameFileName());
      return false;
    }
  }

  //--------------------------------------
  // Recording

  @Override
  public boolean startFrontendRecording() {
    return true;
  }

  @Override
  public boolean startGameRecording(Game game) {
    return launchGame(game, true);
  }

  @Override
  public void endGameRecording(Game game) {
    killEmulators(false);
  }

  @Override
  public void endFrontendRecording() {
  }
}
