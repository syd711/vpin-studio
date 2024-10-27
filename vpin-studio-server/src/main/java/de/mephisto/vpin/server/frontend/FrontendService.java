package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.commons.utils.WinRegistry;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.server.games.*;
import de.mephisto.vpin.server.playlists.Playlist;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FrontendService implements InitializingBean, PreferenceChangedListener {

  private final static Logger LOG = LoggerFactory.getLogger(FrontendService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private Map<String, FrontendConnector> frontendsMap; // autowiring of Frontends

  private final Map<Integer, GameEmulator> emulators = new LinkedHashMap<>();

  public FrontendService(Map<String, FrontendConnector> frontends) {
    this.frontendsMap = frontends;
  }


  public FrontendConnector getFrontendConnector() {
    return getFrontendConnector(getFrontendType());
  }

  public FrontendConnector getFrontendConnector(FrontendType frontendType) {
    return frontendsMap.get(frontendType.name());
  }

  public FrontendType getFrontendType() {
    return systemService.getFrontendType();
  }

  public Frontend getFrontend() {
    return getFrontendConnector().getFrontend();
  }

  public File getFrontendInstallationFolder() {
    return getFrontendConnector().getInstallationFolder();
  }

  //----------------------------------------
  // Access to cached emulators

  public GameEmulator getGameEmulator(int emulatorId) {
    return this.emulators.get(emulatorId);
  }

  public List<GameEmulator> getGameEmulators() {
    return new ArrayList<>(this.emulators.values());
  }

  public List<GameEmulator> getVpxGameEmulators() {
    return this.emulators.values().stream().filter(e -> e.isVpxEmulator()).collect(Collectors.toList());
  }

  public List<GameEmulator> getBackglassGameEmulators() {
    List<GameEmulator> gameEmulators = new ArrayList<>(this.emulators.values());
    return gameEmulators.stream().filter(e -> {
      return e.isVpxEmulator() || e.isFpEmulator();
    }).collect(Collectors.toList());
  }

  public GameEmulator getDefaultGameEmulator() {
    Collection<GameEmulator> values = emulators.values();

    // when there is only one VPX emulator, it is forcibly the default one
    if (values.size() == 1) {
      GameEmulator value = values.iterator().next();
      return value.isVpxEmulator() ? value : null;
    }

    for (GameEmulator value : values) {
      if (value.getDescription() != null && value.isVpxEmulator() && value.getDescription().contains("default")) {
        return value;
      }
    }

    for (GameEmulator value : values) {
      if (value.isVpxEmulator() && value.getNvramFolder().exists()) {
        return value;
      }
      else {
        LOG.error(value + " has no nvram folder \"" + value.getNvramFolder().getAbsolutePath() + "\"");
      }
    }
    LOG.error("Failed to determine emulator for highscores, no VPinMAME/nvram folder could be resolved (" + emulators.size() + " VPX emulators found).");
    return null;
  }

  //-----------------------------------

  public TableDetails getTableDetails(int id) {
    FrontendConnector frontend = getFrontendConnector();
    TableDetails manifest = frontend.getTableDetails(id);
    if (manifest != null) {
      GameEmulator emu = emulators.get(manifest.getEmulatorId());
      manifest.setLauncherList(new ArrayList<>(emu.getAltExeNames()));
    }
    return manifest;

  }

  public void saveTableDetails(int id, TableDetails tableDetails) {
    getFrontendConnector().saveTableDetails(id, tableDetails);
  }

  public void updateTableFileUpdated(int id) {
    getFrontendConnector().updateTableFileUpdated(id);
  }

  public void vpsLink(int gameId, String extTableId, String extTableVersionId) {
    getFrontendConnector().vpsLink(gameId, extTableId, extTableVersionId);
  }

  //--------------------------
  private Game setGameEmulator(Game game) {
    if (game != null) {
      GameEmulator emulator = emulators.get(game.getEmulatorId());
      if (emulator != null) {
        game.setEmulator(emulator);
      }

      FrontendMediaItem frontendMediaItem = getGameMedia(game).getDefaultMediaItem(VPinScreen.Wheel);
      if (frontendMediaItem != null) {
        game.setWheelImage(frontendMediaItem.getFile());
      }
    }
    return game;
  }

  private List<Game> setGameEmulator(List<Game> games) {
    for (Game game : games) {
      setGameEmulator(game);
    }
    return games;
  }

  public Game getGame(int id) {
    return setGameEmulator(getFrontendConnector().getGame(id));
  }

  public Game getGameByFilename(int emulatorId, String filename) {
    return setGameEmulator(getFrontendConnector().getGameByFilename(emulatorId, filename));
  }

  public List<Game> getGamesByEmulator(int emulatorId) {
    return setGameEmulator(getFrontendConnector().getGamesByEmulator(emulatorId));
  }

  public List<Game> getGamesByFilename(String filename) {
    return setGameEmulator(getFrontendConnector().getGamesByFilename(filename));
  }

  public Game getGameByName(int emulatorId, String gameName) {
    return setGameEmulator(getFrontendConnector().getGameByName(emulatorId, gameName));
  }

  public List<Game> getGames() {
    List<Game> results = setGameEmulator(getFrontendConnector().getGames());
    results.sort(Comparator.comparing(Game::getGameDisplayName));
    return results;
  }

  //--------------------------

  // no more used ?
  public int getVersion() {
    return getFrontendConnector().getVersion();
  }

  public JsonSettings getSettings() {
    return getFrontendConnector().getSettings();
  }

  public void saveSettings(@NonNull Map<String, Object> data) {
    getFrontendConnector().saveSettings(data);
  }

  public void setPupPackEnabled(Game game, boolean enable) {
    if (game != null) {
      getFrontendConnector().setPupPackEnabled(game, enable);
    }
  }

  public boolean isPupPackDisabled(Game game) {
    if (game != null) {
      return game.isPupPackDisabled();
    }
    return false;
  }

  public int importGame(@NonNull File file, int emuId) {

    String baseName = FilenameUtils.getBaseName(file.getName());
    String formattedBaseName = baseName;//.replaceAll(" ", "-");
    Game gameByName = getGameByName(emuId, formattedBaseName);
    int count = 1;
    while (gameByName != null) {
      formattedBaseName = FilenameUtils.getBaseName(file.getName()) + count;
      LOG.info("Found existing gamename that exists while importing \"" + file.getName() + "\", trying again with \"" + formattedBaseName + "\"");
      gameByName = getGameByName(emuId, formattedBaseName);
    }

    GameEmulator gameEmulator = emulators.get(emuId);
    String gameFileName = gameEmulator.getGameFileName(file);
    String gameDisplayName = baseName.replaceAll("-", " ").replaceAll("_", " ");
    return getFrontendConnector().importGame(emuId, formattedBaseName, gameFileName, gameDisplayName, null, new Date(file.lastModified()));
  }

  public int importGame(int emulatorId, @NonNull String gameName, @NonNull String gameFileName, @NonNull String gameDisplayName, @Nullable String launchCustomVar, @NonNull java.util.Date dateFileUpdated) {
    return getFrontendConnector().importGame(emulatorId, gameName, gameFileName, gameDisplayName, launchCustomVar, dateFileUpdated);
  }

/*  public boolean deleteGame(int emulatorId, String name) {
    Game gameByFilename = getGameByFilename(emulatorId, name);
    if (gameByFilename != null) {
      return deleteGame(gameByFilename.getId());
    }
    LOG.error("Failed to delete " + name + ": no game entry has been found for this name.");
    return false;
  }*/

  public boolean deleteGame(int id) {
    return getFrontendConnector().deleteGame(id);
  }

  public void deleteGames(int emuId) {
    getFrontendConnector().deleteGames(emuId);
  }

  public int getGameCount() {
    int count = 0;
    for (GameEmulator value : this.emulators.values()) {
      count += getFrontendConnector().getGameCount(value.getId());
    }
    return count;
  }

  public List<Integer> getGameIds() {
    List<Integer> result = new ArrayList<>();
    for (GameEmulator value : this.emulators.values()) {
      result.addAll(getFrontendConnector().getGameIds(value.getId()));
    }
    return result;
  }

  //--------------------------

  @NonNull
  public Playlist getPlayList(int id) {
    return getFrontendConnector().getPlayList(id);
  }

  @NonNull
  public List<Playlist> getPlayLists() {
    return getFrontendConnector().getPlayLists();
  }

  public void setPlaylistColor(int playlistId, long color) {
    getFrontendConnector().setPlaylistColor(playlistId, color);
  }

  public void addToPlaylist(int playlistId, int gameId, int favMode) {
    getFrontendConnector().addToPlaylist(playlistId, gameId, favMode);
  }

  public void updatePlaylistGame(int playlistId, int gameId, int favMode) {
    getFrontendConnector().updatePlaylistGame(playlistId, gameId, favMode);
  }

  public void deleteFromPlaylists(int gameId) {
    getFrontendConnector().deleteFromPlaylists(gameId);
  }

  public void deleteFromPlaylist(int playlistId, int gameId) {
    getFrontendConnector().deleteFromPlaylist(playlistId, gameId);
  }

  //--------------------------

  public java.util.Date getStartDate() {
    return getFrontendConnector().getStartDate();
  }

  @NonNull
  public List<TableAlxEntry> getAlxData() {
    return getFrontendConnector().getAlxData();
  }

  @NonNull
  public List<TableAlxEntry> getAlxData(int gameId) {
    return getFrontendConnector().getAlxData(gameId);
  }

  //--------------------------

  public FrontendControl getPinUPControlFor(VPinScreen screen) {
    switch (screen) {
      case Other2: {
        return getFrontendConnector().getFunction(FrontendControl.FUNCTION_SHOW_OTHER);
      }
      case GameHelp: {
        return getFrontendConnector().getFunction(FrontendControl.FUNCTION_SHOW_HELP);
      }
      case GameInfo: {
        return getFrontendConnector().getFunction(FrontendControl.FUNCTION_SHOW_FLYER);
      }
      default: {
      }
    }

    return new FrontendControl();
  }

  public FrontendControls getControls() {
    return getFrontendConnector().getControls();
  }

  //--------------------------

  @Nullable
  public FrontendPlayerDisplay getFrontendPlayerDisplays(@NonNull VPinScreen screen) {
    List<FrontendPlayerDisplay> pupPlayerDisplays = getFrontendPlayerDisplays();
    for (FrontendPlayerDisplay pupPlayerDisplay : pupPlayerDisplays) {
      VPinScreen vPinScreen = VPinScreen.valueOfScreen(pupPlayerDisplay.getName());
      if (vPinScreen != null && vPinScreen.equals(screen)) {
        return pupPlayerDisplay;
      }
    }
    return null;
  }

  public List<FrontendPlayerDisplay> getFrontendPlayerDisplays() {
    return getFrontendConnector().getFrontendPlayerDisplays();
  }

  public boolean isValidVPXEmulator(Emulator emulator) {
    if (!emulator.isVisualPinball()) {
      return false;
    }

    if (!emulator.isVisible()) {
      LOG.warn("Ignoring " + emulator + ", because the emulator is not visible.");
      return false;
    }

    if (StringUtils.isEmpty(emulator.getDirGames())) {
      LOG.warn("Ignoring " + emulator + ", because \"Games Folder\" is not set.");
      return false;
    }

    // should not ignore as GameEmulator will set a folder by default
    //if (StringUtils.isEmpty(emulator.getDirRoms())) {
    //  LOG.warn("Ignoring " + emulator + ", because \"Roms Folder\" is not set.");
    //  return false;
    //}

    if (getFrontendConnector().getMediaAccessStrategy() != null && StringUtils.isEmpty(emulator.getDirMedia())) {
      LOG.warn("Ignoring " + emulator + ", because \"Media Dir\" is not set.");
      return false;
    }

    return true;
  }

  public void loadEmulators() {
    FrontendConnector frontendConnector = getFrontendConnector();
    frontendConnector.clearCache();
    List<Emulator> ems = frontendConnector.getEmulators();
    this.emulators.clear();
    for (Emulator emulator : ems) {
      try {
        if (!emulator.isVisible()) {
          continue;
        }
        if (!emulator.isEnabled()) {
          continue;
        }

        if (emulator.isVisualPinball() && !isValidVPXEmulator(emulator)) {
          continue;
        }

        GameEmulator gameEmulator = new GameEmulator(emulator);
        emulators.put(emulator.getId(), gameEmulator);

        LOG.info("Loaded Emulator: " + gameEmulator);
      }
      catch (Exception e) {
        LOG.error("Emulator initialization failed: " + e.getMessage(), e);
      }
    }

    if (this.emulators.isEmpty()) {
      LOG.error("*****************************************************************************************");
      LOG.error("No valid game emulators folder, fill all(!) emulator directory settings in your frontend.");
      LOG.error("*****************************************************************************************");
    }
  }

  public void restartFrontend() {
    getFrontendConnector().restartFrontend();
  }

  public boolean isFrontendRunning() {
    return getFrontendConnector().isFrontendRunning();
  }

  public boolean killFrontend() {
    return getFrontendConnector().killFrontend();
  }

  //--------------------------

  @Override
  public void afterPropertiesSet() {

    getFrontendConnector().initializeConnector();

    this.loadEmulators();

    getFrontendConnector().getFrontendPlayerDisplays();

    preferencesService.addChangeListener(this);
  }

  public File getDefaultMediaFolder(@NonNull VPinScreen screen) {
    GameEmulator emu = getDefaultGameEmulator();
    MediaAccessStrategy mediaStrategy = getFrontendConnector().getMediaAccessStrategy();
    return mediaStrategy != null ? mediaStrategy.getEmulatorMediaFolder(emu, screen) : null;
  }

  public File getPlaylistMediaFolder(@NonNull Playlist playList, @NonNull VPinScreen screen) {
    MediaAccessStrategy mediaStrategy = getFrontendConnector().getMediaAccessStrategy();
    return mediaStrategy != null ? mediaStrategy.getPlaylistMediaFolder(playList, screen) : null;
  }

  public File getMediaFolder(@NonNull Game game, @NonNull VPinScreen screen, String extension) {
    MediaAccessStrategy mediaStrategy = getFrontendConnector().getMediaAccessStrategy();
    return mediaStrategy != null ? mediaStrategy.getGameMediaFolder(game, screen, extension) : null;
  }

  @NonNull
  public List<File> getMediaFiles(@NonNull Game game, @NonNull VPinScreen screen) {
    MediaAccessStrategy mediaStrategy = getFrontendConnector().getMediaAccessStrategy();
    if (mediaStrategy != null) {
      return mediaStrategy.getScreenMediaFiles(game, screen);
    }
    return Collections.emptyList();
  }

  public FrontendMediaItem getMediaItem(@NonNull Game game, @NonNull VPinScreen screen, String name) {
    FrontendMedia media = getGameMedia(game);
    return media.getMediaItem(screen, name);
  }

  public FrontendMediaItem getDefaultMediaItem(Game game, VPinScreen screen) {
    FrontendMedia media = getGameMedia(game);
    return media.getDefaultMediaItem(screen);
  }

  @NonNull
  public List<FrontendMediaItem> getMediaItems(@NonNull Game game, @NonNull VPinScreen screen) {
    FrontendMedia media = getGameMedia(game);
    return media.getMediaItems(screen);
  }

  @NonNull
  public FrontendMedia getGameMedia(int gameId) {
    Game game = getGame(gameId);
    return getGameMedia(game);
  }

  @NonNull
  public FrontendMedia getGameMedia(Game game) {
    FrontendMedia frontendMedia = new FrontendMedia();

    List<VPinScreen> screens = getFrontend().getSupportedScreens();
    for (VPinScreen screen : screens) {
      List<FrontendMediaItem> itemList = new ArrayList<>();
      List<File> mediaFiles = getMediaFiles(game, screen);
      for (File file : mediaFiles) {
        FrontendMediaItem item = new FrontendMediaItem(game.getId(), screen, file);
        itemList.add(item);
      }
      frontendMedia.getMedia().put(screen.name(), itemList);
    }
    return frontendMedia;
  }

  public TableAssetsAdapter getTableAssetAdapter() {
    return getFrontendConnector().getTableAssetAdapter();  
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (propertyName.equals(PreferenceNames.PINBALLX_SETTINGS)) {
      getFrontendConnector().initializeConnector();
    }
  }
}