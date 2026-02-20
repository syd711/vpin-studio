package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.restclient.preferences.AutoFillSettings;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.vpx.TableInfo;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.*;
import de.mephisto.vpin.server.playlists.Playlist;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vps.VpsService;
import de.mephisto.vpin.server.vpx.VPXService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.Date;
import java.util.*;
import java.util.List;

@Service
public class FrontendService implements InitializingBean, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private SystemService systemService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private VPXService vpxService;

  @Autowired
  private VpsService vpsService;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private Map<String, FrontendConnector> frontendsMap; // autowiring of Frontends

  @Autowired
  private GameLifecycleService gameLifecycleService;

  @Autowired
  private GameDetailsRepositoryService gameDetailsRepositoryService;

  private FrontendStatusService frontendStatusService;

  private List<FrontendPlayerDisplay> frontendPlayerDisplays;

  public void setFrontendStatusService(FrontendStatusService frontendStatusService) {
    this.frontendStatusService = frontendStatusService;
  }

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

  public String getFrontendName() {
    return getFrontend().getName();
  }

  public File getFrontendInstallationFolder() {
    return getFrontendConnector().getInstallationFolder();
  }

  //----------------------------------------
  // Access to cached emulators

  //-----------------------------------

  public TableDetails getTableDetails(int id) {
    FrontendConnector frontend = getFrontendConnector();
    TableDetails tableDetails = frontend.getTableDetails(id);
    if (tableDetails != null) {
      GameEmulator emu = emulatorService.getGameEmulator(tableDetails.getEmulatorId());
      if (emu != null) {
        tableDetails.setLauncherList(new ArrayList<>(emulatorService.getAltExeNames(emu)));
      }
    }
    return tableDetails;

  }

  public void saveTableDetails(int id, TableDetails tableDetails) {
    TableDetails newTableDetails = getFrontendConnector().saveTableDetails(id, tableDetails);
    gameLifecycleService.notifyGameDataChanged(id, tableDetails, newTableDetails);
  }

  public void updateTableFileUpdated(int id) {
    getFrontendConnector().updateTableFileUpdated(id);
  }

  public void vpsLink(int gameId, String extTableId, String extTableVersionId) {
    getFrontendConnector().vpsLink(gameId, extTableId, extTableVersionId);
  }

  //--------------------------
  @Nullable
  private Game setGameEmulator(Game game) {
    if (game != null) {
      GameEmulator emulator = emulatorService.getGameEmulator(game.getEmulatorId());
      if (emulator != null) {
        game.setEmulator(emulator);
        //  game.setEmulatorName(emulator.getName());
      }
      else {
        LOG.info("No emulator found for {}/{}/{}/{}", game, game.getId(), game.getEmulatorId(), game.getGameFilePath());
        return null;
      }

      //FrontendMediaItem frontendMediaItem = getGameMedia(game).getDefaultMediaItem(VPinScreen.Wheel);
      //if (frontendMediaItem != null) {
      //  game.setWheelImage(frontendMediaItem.getFile());
      //}
    }
    return game;
  }

  private List<Game> setGameEmulator(List<Game> games) {
    for (Game game : games) {
      setGameEmulator(game);
    }
    return games;
  }

  /**
   * Returns the original game from the frontend, without any custom field replacements.
   * <b>Important:</b> This instance may not have any ROM name or not the correct one!
   *
   * @param id the id of the game
   * @return the original un-customized game instance
   */
  public Game getOriginalGame(int id) {
    Game game = getFrontendConnector().getGame(id);
    return setGameEmulator(game);
  }

  public Game getGameByFilename(int emulatorId, String filename) {
    return setGameEmulator(getFrontendConnector().getGameByFilename(emulatorId, filename));
  }

  /**
   * Same as getGameByFilename() but filename has no extension
   * Usefull to derive a game from a backglass name
   */
  public Game getGameByBaseFilename(int emulatorId, String filename) {
    GameEmulator emu = emulatorService.getGameEmulator(emulatorId);
    String gameFilename = filename + "." + emu.getGameExt();
    return getGameByFilename(emulatorId, gameFilename);
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

  public int getVersion() {
    return getFrontendConnector().getVersion();
  }

  public JsonSettings getSettings() {
    return getFrontendConnector().getSettings();
  }

  public void saveSettings(JsonSettings settings) {
    try {
      String serialize = settings.toJson();
      @SuppressWarnings("unchecked")
      Map<String, Object> data = JsonSettings.objectMapper.readValue(serialize, HashMap.class);
      saveSettings(data);
    }
    catch(IOException ioe) {
      LOG.error("Cannot save settings", ioe);
    }
  }

  public void saveSettings(@NonNull Map<String, Object> data) {
    getFrontendConnector().saveSettings(data);
  }

  public boolean setPupPackEnabled(Game game, boolean enable) {
    if (game != null) {
      getFrontendConnector().setPupPackEnabled(game, enable);
      return enable;
    }
    return false;
  }

  @NonNull
  public TableDetails autoFill(Game game, TableDetails tableDetails, boolean simulate) {
    String vpsTableId = game.getExtTableId();
    String vpsTableVersionId = game.getExtTableVersionId();
    return autoFill(game, tableDetails, vpsTableId, vpsTableVersionId, simulate);
  }

  @NonNull
  public TableDetails autoFill(Game game, TableDetails tableDetails, String vpsTableId, String vpsTableVersionId, boolean simulate) {
    TableInfo tableInfo = vpxService.getTableInfo(game);

    AutoFillSettings autoFillSettings = preferencesService.getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class).getAutoFillSettings();
    boolean overwrite = autoFillSettings.isOverwrite();

    if (!StringUtils.isEmpty(vpsTableId)) {
      VpsTable vpsTable = vpsService.getTableById(vpsTableId);
      if (vpsTable != null) {

        if (autoFillSettings.isGameYear()) {
          if (vpsTable.getYear() > 0 && (tableDetails.getGameYear() == null || tableDetails.getGameYear() == 0 || overwrite)) {
            tableDetails.setGameYear(vpsTable.getYear());
          }
        }

        if (autoFillSettings.isNumberOfPlayers()) {
          if (vpsTable.getPlayers() == 0 || overwrite) {
            tableDetails.setNumberOfPlayers(vpsTable.getPlayers());
          }
        }

        if (autoFillSettings.isIpdbNumber()) {
          if (!StringUtils.isEmpty(vpsTable.getIpdbUrl())) {
            if (StringUtils.isEmpty(tableDetails.getUrl()) || overwrite) {
              tableDetails.setUrl(vpsTable.getIpdbUrl());
            }

            String url = vpsTable.getIpdbUrl();
            if (url.contains("id=")) {
              if (StringUtils.isEmpty(tableDetails.getIPDBNum()) || overwrite) {
                tableDetails.setIPDBNum(url.substring(url.indexOf("id=") + 3));
              }
            }
          }
        }

        if (autoFillSettings.isGameTheme()) {
          if (vpsTable.getTheme() != null && !vpsTable.getTheme().isEmpty() && (StringUtils.isEmpty(tableDetails.getGameTheme()) || overwrite)) {
            tableDetails.setGameTheme(String.join(",", vpsTable.getTheme()));
          }
        }


        if (autoFillSettings.isDesignBy()) {
          if (vpsTable.getDesigners() != null && !vpsTable.getDesigners().isEmpty() && (StringUtils.isEmpty(tableDetails.getDesignedBy()) || overwrite)) {
            tableDetails.setDesignedBy(String.join(",", vpsTable.getDesigners()));
          }
        }

        if (autoFillSettings.isManufacturer()) {
          if (!StringUtils.isEmpty(vpsTable.getManufacturer()) && (StringUtils.isEmpty(tableDetails.getManufacturer()) || overwrite)) {
            tableDetails.setManufacturer(vpsTable.getManufacturer());
          }
        }

        if (autoFillSettings.isGameType()) {
          if (!StringUtils.isEmpty(vpsTable.getType())) {
            try {
              String gameType = vpsTable.getType();
              if (!StringUtils.isEmpty(gameType) && (StringUtils.isEmpty(tableDetails.getGameType()) || overwrite)) {
                tableDetails.setGameType(gameType);
              }
            }
            catch (Exception e) {
              //ignore
            }
          }
        }

        if (!StringUtils.isEmpty(vpsTableVersionId)) {
          VpsTableVersion tableVersion = vpsTable.getTableVersionById(vpsTableVersionId);
          if (tableVersion != null) {
            if (autoFillSettings.isGameVersion()) {
              if (!StringUtils.isEmpty(tableVersion.getVersion()) && (StringUtils.isEmpty(tableDetails.getGameVersion()) || overwrite)) {
                tableDetails.setGameVersion(tableVersion.getVersion());
              }
            }

            if (autoFillSettings.isAuthor()) {
              List<String> authors = tableVersion.getAuthors();
              if (authors != null && !authors.isEmpty() && (StringUtils.isEmpty(tableDetails.getAuthor()) || overwrite)) {
                tableDetails.setAuthor(String.join(", ", authors));
              }
            }

            if (autoFillSettings.isDetails()) {
              StringBuilder details = new StringBuilder();
              if (!StringUtils.isEmpty(tableVersion.getComment())) {
                details.append("VPS Comment:\n");
                details.append(tableVersion.getComment());
              }
              if (tableInfo != null && !StringUtils.isEmpty(tableInfo.getTableDescription())) {
                String tableDescription = tableInfo.getTableDescription();
                details.append("\n\n");
                details.append(tableDescription);
              }

              if (StringUtils.isEmpty(tableDetails.getgDetails()) || overwrite) {
                tableDetails.setgDetails(details.toString());
              }

            }

            if (autoFillSettings.isNotes()) {
              if (tableInfo != null && !StringUtils.isEmpty(tableInfo.getTableRules()) && (StringUtils.isEmpty(tableDetails.getgNotes()) || overwrite)) {
                tableDetails.setgNotes(tableInfo.getTableRules());
              }
            }

            if (autoFillSettings.isTags()) {
              if (tableVersion.getFeatures() != null && !tableVersion.getFeatures().isEmpty()) {
                String tags = String.join(", ", tableVersion.getFeatures());
                String tableDetailTags = tableDetails.getTags() != null ? tableDetails.getTags() : "";
                if (!tableDetailTags.contains(tags)) {
                  tableDetailTags = tableDetailTags + ", " + tags;
                }

                if (StringUtils.isEmpty(tableDetails.getTags()) || overwrite) {
                  tableDetails.setTags(tableDetailTags);
                }
              }
            }

            LOG.info("Auto-applied VPS table version \"" + tableVersion + "\" (" + tableVersion.getId() + ")");
          }
        }
        else {
          fillTableInfoWithVpxData(tableInfo, game, tableDetails, autoFillSettings);
        }
      }
    }
    else {
      fillTableInfoWithVpxData(tableInfo, game, tableDetails, autoFillSettings);
    }

    if (simulate) {
      LOG.info("Finished simulated auto-fill for \"" + game.getGameDisplayName() + "\"");
    }
    else {
      saveTableDetails(game.getId(), tableDetails);
      LOG.info("Finished auto-fill for \"" + game.getGameDisplayName() + "\"");
    }

    return tableDetails;
  }

  public int importGame(File file, boolean importToFrontend, int playListId, int emuId) {
    if (importToFrontend) {
      int gameId = importGame(file, emuId);
      if (gameId >= 0 && playListId >= 0) {
        addToPlaylist(playListId, gameId, 0);
      }
      return gameId;
    }
    return -1;
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
      count++;
    }

    GameEmulator gameEmulator = emulatorService.getGameEmulator(emuId);
    String gameFileName = gameEmulator.getGameFileName(file);
    String gameDisplayName = baseName.replaceAll("-", " ").replaceAll("_", " ");

    TableDetails tableDetails = new TableDetails();
    tableDetails.setEmulatorId(emuId);
    tableDetails.setStatus(1);
    tableDetails.setGameName(formattedBaseName);
    tableDetails.setGameFileName(gameFileName);
    tableDetails.setGameDisplayName(gameDisplayName);
    tableDetails.setDateModified(new Date(file.lastModified()));
    return importGame(tableDetails);
  }

  public int importGame(@NonNull TableDetails tableDetails) {
    return getFrontendConnector().importGame(tableDetails);
  }

  public boolean deleteGame(int id) {
    return getFrontendConnector().deleteGame(id);
  }

  public void deleteGames(int emuId) {
    getFrontendConnector().deleteGames(emuId);
  }

  public List<Integer> getGameIds() {
    List<Integer> result = new ArrayList<>();
    List<GameEmulator> validGameEmulators = emulatorService.getValidGameEmulators();
    for (GameEmulator value : validGameEmulators) {
      result.addAll(getFrontendConnector().getGameIds(value.getId()));
    }
    return result;
  }

  //--------------------------

  @NonNull
  public Playlist getPlayList(int id) {
    return getFrontendConnector().getPlaylist(id);
  }

  @NonNull
  public Playlist clearPlayList(int id) {
    return getFrontendConnector().clearPlaylist(id);
  }

  @NonNull
  public List<Playlist> getPlaylists() {
    return getFrontendConnector().getPlaylists();
  }

  @NonNull
  public Playlist getPlaylistTree() {
    return getFrontendConnector().getPlaylistTree();
  }

  public boolean deletePlaylist(int playlistId) {
    return getFrontendConnector().deletePlaylist(playlistId);
  }

  public Playlist savePlaylist(Playlist playlist) {
    return getFrontendConnector().savePlaylist(playlist);
  }

  public void savePlaylistOrder(PlaylistOrder playlistOrder) {
    getFrontendConnector().savePlaylistOrder(playlistOrder);
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
        return getFrontendConnector().getFrontendControl(FrontendControl.FUNCTION_SHOW_OTHER);
      }
      case GameHelp: {
        return getFrontendConnector().getFrontendControl(FrontendControl.FUNCTION_SHOW_HELP);
      }
      case GameInfo: {
        return getFrontendConnector().getFrontendControl(FrontendControl.FUNCTION_SHOW_FLYER);
      }
      default: {
      }
    }

    return null;
  }

  public FrontendControls getControls() {
    return getFrontendConnector().getControls();
  }

  //--------------------------

  public List<FrontendPlayerDisplay> getFrontendPlayerDisplays(boolean forceReload) {
    if (frontendPlayerDisplays == null || forceReload) {
      frontendPlayerDisplays = getFrontendConnector().getFrontendPlayerDisplays();
    }
    return frontendPlayerDisplays;
  }

  public boolean isFrontendRunning() {
    return getFrontendConnector().isFrontendRunning();
  }

  public boolean killFrontend() {
    getFrontendConnector().killFrontend();
    if (frontendStatusService != null) {
      frontendStatusService.notifyFrontendExit();
    }
    return true;
  }

  //--------------------------

  public File getDefaultMediaFolder(@NonNull VPinScreen screen) {
    List<GameEmulator> vpxGameEmulators = emulatorService.getVpxGameEmulators();
    if (vpxGameEmulators.isEmpty()) {
      return getFrontendInstallationFolder();
    }
    GameEmulator emulator = vpxGameEmulators.get(0);
    MediaAccessStrategy mediaStrategy = getFrontendConnector().getMediaAccessStrategy();
    return mediaStrategy != null ? mediaStrategy.getEmulatorMediaFolder(emulator, screen) : null;
  }

  public File getPlaylistMediaFolder(@NonNull Playlist playList, @NonNull VPinScreen screen, boolean create) {
    MediaAccessStrategy mediaStrategy = getFrontendConnector().getMediaAccessStrategy();
    return mediaStrategy != null ? mediaStrategy.getPlaylistMediaFolder(playList, screen, create) : null;
  }

  public File getMediaFolder(@NonNull Game game, @NonNull VPinScreen screen, @Nullable String extension, boolean create) {
    MediaAccessStrategy mediaStrategy = getFrontendConnector().getMediaAccessStrategy();
    return mediaStrategy != null ? mediaStrategy.getGameMediaFolder(game, screen, extension, create) : null;
  }

  public boolean deletePlaylistMediaFolder(@NonNull Playlist playList, @NonNull VPinScreen screen, @Nullable String extension) {
    MediaAccessStrategy mediaStrategy = getFrontendConnector().getMediaAccessStrategy();
    if (mediaStrategy != null) {
      File mediaFolder = mediaStrategy.getPlaylistMediaFolder(playList, screen, false);
      mediaStrategy.stopMonitoring(mediaFolder);
      return FileUtils.deleteFolder(mediaFolder);
    }
    return false;
  }

  public boolean deleteMediaFolder(@NonNull Game game, @NonNull VPinScreen screen, @Nullable String extension) {
    MediaAccessStrategy mediaStrategy = getFrontendConnector().getMediaAccessStrategy();
    if (mediaStrategy != null) {
      File mediaFolder = mediaStrategy.getGameMediaFolder(game, screen, extension, false);
      mediaStrategy.stopMonitoring(mediaFolder);
      return FileUtils.deleteFolder(mediaFolder);
    }
    return false;
  }

  @NonNull
  public List<File> getMediaFiles(@NonNull Game game, @NonNull VPinScreen screen) {
    MediaAccessStrategy mediaStrategy = getFrontendConnector().getMediaAccessStrategy();
    String mediaSearchTerm = game.getMediaSearch();
    return mediaStrategy != null ? mediaStrategy.getScreenMediaFiles(game, screen, mediaSearchTerm) : Collections.emptyList();
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
    List<FrontendMediaItem> itemList = new ArrayList<>();
    List<File> mediaFiles = getMediaFiles(game, screen);
    for (File file : mediaFiles) {
      FrontendMediaItem item = FrontendMediaItem.forGame(game.getId(), screen, file);
      itemList.add(item);
    }
    return itemList;
  }

  public File getWheelImage(Game game) {
    List<File> mediaFiles = getMediaFiles(game, VPinScreen.Wheel);
    return mediaFiles.isEmpty() ? null : mediaFiles.get(0);
  }

  /**
   * Launches the given game through the frontend.
   *
   * @param game
   * @return
   */
  public boolean launchGame(Game game) {
    if (game != null) {
      systemService.setMaintenanceMode(false);
      return getFrontendConnector().launchGame(game);
    }
    return false;
  }

  public void restartFrontend() {
    getFrontendConnector().restartFrontend();
  }

  @NonNull
  public FrontendMedia getGameMedia(int gameId) {
    Game game = getOriginalGame(gameId);
    if (game != null) {
      return getGameMedia(game);
    }
    return new FrontendMedia();
  }

  public boolean clearCache() {
    this.frontendPlayerDisplays = null;
    return true;
  }

  @NonNull
  public FrontendMedia getGameMedia(Game game) {
    FrontendMedia frontendMedia = new FrontendMedia();

    List<VPinScreen> screens = getFrontend().getSupportedScreens();
    for (VPinScreen screen : screens) {
      List<FrontendMediaItem> itemList = new ArrayList<>();
      List<File> mediaFiles = getMediaFiles(game, screen);
      for (File file : mediaFiles) {
        FrontendMediaItem item = FrontendMediaItem.forGame(game.getId(), screen, file);
        itemList.add(item);
      }
      // compare filenames ignoring case
      Collections.sort(itemList, (i1, i2) -> i1.getName().compareToIgnoreCase(i2.getName()));
      frontendMedia.getMedia().put(screen.name(), itemList);
    }
    return frontendMedia;
  }

  public TableAssetsAdapter<Game> getTableAssetAdapter() {
    return getFrontendConnector().getTableAssetAdapter();
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (propertyName.equals(PreferenceNames.PINBALLX_SETTINGS)) {
      getFrontendConnector().initializeConnector();
    }
  }

  /**
   * Some fallback: we use the VPX script metadata if the VPS version data has not been applied.
   */
  private void fillTableInfoWithVpxData(TableInfo tableInfo, @NonNull Game game, @NonNull TableDetails tableDetails, @NonNull AutoFillSettings autoFillSettings) {
    boolean overwrite = autoFillSettings.isOverwrite();
    if (tableInfo != null) {
      if (autoFillSettings.isGameVersion()) {
        if (!StringUtils.isEmpty(tableInfo.getTableVersion()) && (StringUtils.isEmpty(tableDetails.getGameVersion()) || overwrite)) {
          tableDetails.setGameVersion(tableInfo.getTableVersion());
        }
      }

      if (autoFillSettings.isAuthor()) {
        if (!StringUtils.isEmpty(tableInfo.getAuthorName()) && (StringUtils.isEmpty(tableDetails.getAuthor()) || overwrite)) {
          tableDetails.setAuthor(tableInfo.getAuthorName());
        }
      }
    }
  }

  public GameEmulator saveEmulator(GameEmulator emulator) {
    return getFrontendConnector().saveEmulator(emulator);
  }

  public boolean deleteEmulator(int emulatorId) {
    List<Game> gamesByEmulator = getFrontendConnector().getGamesByEmulator(emulatorId);
    killFrontend();
    boolean result = getFrontendConnector().deleteEmulator(emulatorId);
    if (result) {
      LOG.info("Sucessfully deleted emulator {}, now deleting Studio game details.", emulatorId);
      for (Game game : gamesByEmulator) {
        GameDetails byPupId = gameDetailsRepositoryService.findByPupId(game.getId());
        if (byPupId != null) {
          gameDetailsRepositoryService.delete(byPupId);
        }
      }
      LOG.info("Studio game details deletion completed, deleted {} games", gamesByEmulator.size());
    }
    return result;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      emulatorService.setFrontendService(this);
      getFrontendConnector().initializeConnector();

      long start = System.currentTimeMillis();
      LOG.info("Initializing emulators");
      emulatorService.loadEmulators();
      LOG.info("Initial emulator load took {}ms", (System.currentTimeMillis() - start));

      getFrontendConnector().getFrontendPlayerDisplays();
      preferencesService.addChangeListener(this);

      boolean isHeadless = GraphicsEnvironment.isHeadless();
      if (!isHeadless) {
        List<FrontendPlayerDisplay> displays = getFrontendPlayerDisplays(false);
        LOG.info("########################## Frontend Screen Summary #####################################");
        for (FrontendPlayerDisplay frontendPlayerDisplay : displays) {
          LOG.info(frontendPlayerDisplay.toString());
        }
        LOG.info("######################### /Frontend Screen Summary #####################################");
      }
    }
    catch (Exception e) {
      LOG.info("FrontendService initialization failed: {}", e.getMessage(), e);
    }
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}