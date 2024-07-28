package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.TableManagerSettings;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.restclient.games.GameList;
import de.mephisto.vpin.restclient.games.GameListItem;
import de.mephisto.vpin.restclient.games.GameVpsMatch;
import de.mephisto.vpin.restclient.preferences.AutoFillSettings;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.vpx.TableInfo;
import de.mephisto.vpin.server.games.*;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.cards.CardService;
import de.mephisto.vpin.server.listeners.EventOrigin;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vps.VpsService;
import de.mephisto.vpin.server.vpx.VPXService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FrontendStatusService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(FrontendStatusService.class);

  private final List<TableStatusChangeListener> tableStatusChangeListeners = new ArrayList<>();
  private final List<FrontendStatusChangeListener> frontendStatusChangeListeners = new ArrayList<>();

  @Autowired
  private SystemService systemService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private GameService gameService;

  @Autowired
  private VPXService vpxService;

  @Autowired
  private CardService cardService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private VpsService vpsService;

  public FrontendControl getPinUPControlFor(VPinScreen screen) {
    return frontendService.getPinUPControlFor(screen);
  }

  public FrontendControls getPinUPControls() {
    return frontendService.getControls();
  }

  public void addTableStatusChangeListener(TableStatusChangeListener listener) {
    this.tableStatusChangeListeners.add(listener);
  }

  public void addFrontendStatusChangeListener(FrontendStatusChangeListener listener) {
    this.frontendStatusChangeListeners.add(listener);
  }

  public GameList getImportTables(int emuId) {
    GameEmulator emulator = frontendService.getGameEmulator(emuId);
    GameList list = new GameList();
    File vpxTablesFolder = emulator.getTablesFolder();

    List<File> files = new ArrayList<>();
    if (emulator.isVpxEmulator()) {
      files.addAll(FileUtils.listFiles(vpxTablesFolder, new String[]{"vpx"}, true));
    }
    else if (emulator.isFpEmulator()) {
      files.addAll(FileUtils.listFiles(vpxTablesFolder, new String[]{"fpt"}, true));
    }

    List<Game> games = frontendService.getGamesByEmulator(emulator.getId());
    List<String> emulatorGameFileNames = games.stream().map(Game::getGameFileName).collect(Collectors.toList());
    for (File file : files) {
      String gameFileName = emulator.getGameFileName(file);
      if (!emulatorGameFileNames.contains(gameFileName)) {
        GameListItem item = new GameListItem();
        item.setName(file.getName());
        item.setFileName(file.getAbsolutePath());
        item.setEmuId(emulator.getId());
        list.getItems().add(item);
      }
    }
    Collections.sort(list.getItems(), Comparator.comparing(o -> o.getName().toLowerCase()));
    return list;
  }

  public int importVPXGame(File file, boolean importToFrontend, int playListId, int emuId) {
    if (importToFrontend) {
      int gameId = frontendService.importGame(file, emuId);
      if (gameId >= 0 && playListId >= 0) {
        frontendService.addToPlaylist(playListId, gameId, 0);
      }
      return gameId;
    }
    return -1;
  }

  public void notifyTableStatusChange(final Game game, final boolean started, TableStatusChangedOrigin origin) {
    TableStatusChangedEvent event = new TableStatusChangedEvent() {
      @NotNull
      @Override
      public Game getGame() {
        return game;
      }

      @Override
      public TableStatusChangedOrigin getOrigin() {
        return origin;
      }
    };

    for (TableStatusChangeListener listener : this.tableStatusChangeListeners) {
      if (started) {
        listener.tableLaunched(event);
      }
      else {
        listener.tableExited(event);
      }
    }
  }

  public boolean terminate() {
    boolean b = frontendService.killFrontend();
    for (FrontendStatusChangeListener listener : frontendStatusChangeListeners) {
      listener.frontendExited();
    }
    return b;
  }

  public void notifyFrontendLaunch() {
    for (FrontendStatusChangeListener listener : frontendStatusChangeListeners) {
      listener.frontendLaunched();
    }
  }

  public void notifyFrontendRestart() {
    for (FrontendStatusChangeListener listener : frontendStatusChangeListeners) {
      listener.frontendRestarted();
    }
  }

  public void notifyFrontendExit() {
    for (FrontendStatusChangeListener listener : frontendStatusChangeListeners) {
      listener.frontendExited();
    }
  }

  /**
   * moved from VpsService to break circular dependency.
   */
  public GameVpsMatch autoMatch(Game game, boolean overwrite, boolean simulate) {
    GameVpsMatch vpsMatch = vpsService.autoMatch(game, overwrite);
    if (vpsMatch != null && !simulate) {
      vpsLink(game.getId(), vpsMatch.getExtTableId(), vpsMatch.getExtTableVersionId());
      if (StringUtils.isNotEmpty(vpsMatch.getVersion())) {
        fixGameVersion(game.getId(), vpsMatch.getVersion(), false);
      }
    }
    return vpsMatch;
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
          if (vpsTable.getPlayers() > 0 && (tableDetails.getNumberPlays() == null || tableDetails.getNumberPlays() == 0 || overwrite)) {
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
      frontendService.saveTableDetails(game.getId(), tableDetails);
      LOG.info("Finished auto-fill for \"" + game.getGameDisplayName() + "\"");
    }

    return tableDetails;
  }

  public void vpsLink(int gameId, String extTableId, String extTableVersionId) {
    // keep track of the match in the internal database
    if (gameService.vpsLink(gameId, extTableId, extTableVersionId)) {
      // update the table in the frontend
      frontendService.vpsLink(gameId, extTableId, extTableVersionId);
    }
  }

  public void fixGameVersion(int gameId, String version, boolean overwrite) {
    // keep track of the version  in the internal database
    if (gameService.fixVersion(gameId, version, overwrite)) {
      // update the table in the frontend
      TableDetails tableDetails = getTableDetails(gameId);
      if (tableDetails != null) {
        tableDetails.setGameVersion(version);
        saveTableDetails(tableDetails, gameId, false);
      }
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

  public TableDetails getTableDetails(int gameId) {
    return frontendService.getTableDetails(gameId);
  }

  public void updateTableFileUpdated(int gameId) {
    frontendService.updateTableFileUpdated(gameId);
  }

  public TableDetails saveTableDetails(TableDetails updatedTableDetails, int gameId, boolean renamingChecks) {
    //fetch existing data first
    TableDetails oldDetails = getTableDetails(gameId);
    Game game = frontendService.getGame(gameId);

    //fix input and save input
    String gameFilename = updatedTableDetails.getGameFileName();
    if (game.isVpxGame() && !gameFilename.endsWith(".vpx")) {
      gameFilename = gameFilename + ".vpx";
      updatedTableDetails.setGameFileName(gameFilename);
    }

    gameService.fixVersion(gameId, updatedTableDetails.getGameVersion(), true);
    frontendService.saveTableDetails(gameId, updatedTableDetails);

    //for upload and replace, we do not need any renaming
    if (game.isVpxGame()) {
      if (!renamingChecks) {
        runHighscoreRefreshCheck(game, oldDetails, updatedTableDetails);
        return updatedTableDetails;
      }

      //rename game filename which results in renaming VPX related files
      if (!updatedTableDetails.getGameFileName().equals(oldDetails.getGameFileName())) {
        String name = FilenameUtils.getBaseName(updatedTableDetails.getGameFileName());
        String existingName = FilenameUtils.getBaseName(game.getGameFile().getName());
        if (!existingName.equalsIgnoreCase(name)) {
          if (game.getGameFile().exists()) {
            de.mephisto.vpin.commons.utils.FileUtils.renameToBaseName(game.getGameFile(), name);
          }

          if (game.getDirectB2SFile().exists()) {
            de.mephisto.vpin.commons.utils.FileUtils.renameToBaseName(game.getDirectB2SFile(), name);
          }

          if (game.getPOVFile().exists()) {
            de.mephisto.vpin.commons.utils.FileUtils.renameToBaseName(game.getPOVFile(), name);
          }

          if (game.getResFile().exists()) {
            de.mephisto.vpin.commons.utils.FileUtils.renameToBaseName(game.getResFile(), name);
          }

          if (game.getIniFile().exists()) {
            de.mephisto.vpin.commons.utils.FileUtils.renameToBaseName(game.getIniFile(), name);
          }

          if (game.getVBSFile().exists()) {
            de.mephisto.vpin.commons.utils.FileUtils.renameToBaseName(game.getVBSFile(), name);
          }
          LOG.info("Finished game file renaming from \"" + oldDetails.getGameFileName() + "\" to \"" + updatedTableDetails.getGameFileName() + "\"");
        }
        else {
          //revert to old value
          updatedTableDetails.setGameFileName(oldDetails.getGameFileName());
          frontendService.saveTableDetails(gameId, updatedTableDetails);
          LOG.info("Renaming game file from \"" + oldDetails.getGameFileName() + "\" to \"" + updatedTableDetails.getGameFileName() + "\" failed, VPX renaming failed.");
        }
      }
    }


    //rename the game name, which results in renaming all assets
    if (!updatedTableDetails.getGameName().equals(oldDetails.getGameName())) {
      renameGameMedia(game, oldDetails.getGameName(), updatedTableDetails.getGameName());
    }

    if (game.isVpxGame()) {
      runHighscoreRefreshCheck(game, oldDetails, updatedTableDetails);
    }

    return updatedTableDetails;
  }

  public void runHighscoreRefreshCheck(Game game, TableDetails oldDetails, TableDetails newDetails) {
    boolean romChanged = !StringUtils.equalsIgnoreCase(oldDetails.getRomName(), newDetails.getRomName());
    boolean hsChanged = !StringUtils.equalsIgnoreCase(oldDetails.getHsFilename(), newDetails.getHsFilename());

    if (romChanged || hsChanged) {
      LOG.info("Game highscore data fields have been changed, triggering score check.");
      highscoreService.scanScore(game, EventOrigin.USER_INITIATED);
      cardService.generateCard(game);
    }
  }

  public boolean restart() {
    frontendService.restartFrontend();
    return true;
  }

  public void augmentWheel(Game game, String badge) {
    FrontendMediaItem frontendMediaItem = game.getGameMedia().getDefaultMediaItem(VPinScreen.Wheel);
    if (frontendMediaItem != null) {
      File wheelIcon = frontendMediaItem.getFile();
      WheelAugmenter augmenter = new WheelAugmenter(wheelIcon);

      File badgeFile = systemService.getBadgeFile(badge);
      if (badgeFile.exists()) {
        augmenter.augment(badgeFile);
      }
    }
  }

  public void deAugmentWheel(Game game) {
    FrontendMediaItem frontendMediaItem = game.getGameMedia().getDefaultMediaItem(VPinScreen.Wheel);
    if (frontendMediaItem != null) {
      File wheelIcon = frontendMediaItem.getFile();
      WheelAugmenter augmenter = new WheelAugmenter(wheelIcon);
      augmenter.deAugment();
    }
  }

  @NonNull
  public TableManagerSettings getArchiveManagerDescriptor() {
    TableManagerSettings descriptor = new TableManagerSettings();
    File file = systemService.getVPinStudioMenuExe();
    Game game = frontendService.getGameByFilename(file.getAbsolutePath());
    if (game != null) {
      descriptor.setPlaylistId(frontendService.getPlayListForGame(game.getId()).getId());
    }
    return descriptor;
  }

  public void cloneGameMedia(Game original, Game clone) {
    VPinScreen[] values = VPinScreen.values();
    for (VPinScreen originalScreenValue : values) {
      try {
        List<FrontendMediaItem> frontendMediaItems = original.getGameMedia().getMediaItems(originalScreenValue);
        for (FrontendMediaItem frontendMediaItem : frontendMediaItems) {
          if (frontendMediaItem.getFile().exists()) {
            File mediaFile = frontendMediaItem.getFile();
            String suffix = FilenameUtils.getExtension(mediaFile.getName());
            File cloneTarget = new File(clone.getMediaFolder(originalScreenValue), clone.getGameName() + "." + suffix);
            if (mediaFile.getName().equals(cloneTarget.getName())) {
              LOG.warn("Source name and target name of media asset " + mediaFile.getAbsolutePath() + " are identical, skipping cloning.");
              return;
            }

            if (cloneTarget.exists() && !cloneTarget.delete()) {
              LOG.error("Failed to clone media asset " + cloneTarget.getAbsolutePath() + ": deletion of existing asset failed.");
              return;
            }
            FileUtils.copyFile(mediaFile, cloneTarget);
            LOG.info("Cloned media asset: " + mediaFile.getAbsolutePath() + " to " + cloneTarget.getAbsolutePath());
          }
        }
      }
      catch (IOException e) {
        LOG.info("Failed to clone media asset: " + e.getMessage(), e);
      }
    }
  }

  public void renameGameMedia(Game game, String oldBaseName, String newBaseName) {
    VPinScreen[] values = VPinScreen.values();
    int assetRenameCounter = 0;
    for (VPinScreen screen : values) {
      List<FrontendMediaItem> frontendMediaItems = game.getGameMedia().getMediaItems(screen);
      for (FrontendMediaItem frontendMediaItem : frontendMediaItems) {
        File gameMediaFile = frontendMediaItem.getFile();
        if (gameMediaFile.exists()) {
          if (screen.equals(VPinScreen.Wheel)) {
            WheelAugmenter augmenter = new WheelAugmenter(gameMediaFile);
            augmenter.deAugment();
          }

          if (de.mephisto.vpin.commons.utils.FileUtils.assetRename(gameMediaFile, oldBaseName, newBaseName)) {
            assetRenameCounter++;
            LOG.info("[" + screen + "] Renamed media asset from \"" + gameMediaFile.getName() + "\" to name \"" + newBaseName + "\"");
          }
          else {
            LOG.warn("[" + screen + "] Renaming media asset from \"" + gameMediaFile.getName() + "\" to name \"" + newBaseName + "\" failed.");
          }
        }
      }
    }
    LOG.info("Finished asset renaming for \"" + oldBaseName + "\" to \"" + newBaseName + "\", renamed " + assetRenameCounter + " assets.");
  }

  public JsonSettings getSettings() {
    return frontendService.getSettings();
  }

  public boolean saveSettings(Map<String, Object> options) {
    frontendService.saveSettings(options);
    return true;
  }

  @NonNull
  public List<GameEmulator> getGameEmulators() {
    return frontendService.getGameEmulators();
  }

  @NonNull
  public List<GameEmulator> getBackglassGameEmulators() {
    return frontendService.getBackglassGameEmulators();
  }

  @Nullable
  public GameEmulator getGameEmulator(int id) {
    return frontendService.getGameEmulator(id);
  }

  public int getVersion() {
    return frontendService.getVersion();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Thread shutdownHook = new Thread(this::notifyFrontendExit);
    Runtime.getRuntime().addShutdownHook(shutdownHook);
  }
}
