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
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.vpx.TableInfo;
import de.mephisto.vpin.server.frontend.popper.WheelAugmenter;
import de.mephisto.vpin.server.games.*;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.cards.CardService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
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
public class FrontendStatusService implements InitializingBean, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(FrontendStatusService.class);

  private final List<FrontendStatusChangeListener> listeners = new ArrayList<>();

  @Autowired
  private SystemService systemService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private GameService gameService;

  @Autowired
  private VPXService vpxService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private CardService cardService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private VpsService vpsService;

  private ServerSettings serverSettings;

  public FrontendControl getPinUPControlFor(VPinScreen screen) {
    return frontendService.getPinUPControlFor(screen);
  }

  public FrontendControls getPinUPControls() {
    return frontendService.getControls();
  }

  @SuppressWarnings("unused")
  public void addFrontendStatusChangeListener(FrontendStatusChangeListener listener) {
    this.listeners.add(listener);
  }

  public GameList getImportTables() {
    List<GameEmulator> emulators = frontendService.getVpxGameEmulators();
    GameList list = new GameList();
    for (GameEmulator emulator : emulators) {
      File vpxTablesFolder = emulator.getTablesFolder();
      List<File> files = new ArrayList<>(FileUtils.listFiles(vpxTablesFolder, new String[]{"vpx"}, true));
      List<Game> games = frontendService.getGames();
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
    }
    Collections.sort(list.getItems(), Comparator.comparing(o -> o.getName().toLowerCase()));
    return list;
  }

  public int importVPXGame(File file, boolean importToPopper, int playListId, int emuId) {
    if (importToPopper) {
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

    for (FrontendStatusChangeListener listener : this.listeners) {
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
    for (FrontendStatusChangeListener listener : listeners) {
      listener.frontendExited();
    }
    return b;
  }

  public void notifyFrontendLaunch() {
    for (FrontendStatusChangeListener listener : listeners) {
      listener.frontendLaunched();
    }
  }

  public void notifyFrontendRestart() {
    for (FrontendStatusChangeListener listener : listeners) {
      listener.frontendRestarted();
    }
  }

  public void notifyFrontendExit() {
    for (FrontendStatusChangeListener listener : listeners) {
      listener.frontendExited();
    }
  }

  /**
   * moved from VpsService to break circular dependency.
   */
  public GameVpsMatch autoMatch(Game game, boolean overwrite) {
    GameVpsMatch vpsMatch = vpsService.autoMatch(game, overwrite);
    if (vpsMatch != null) {
      vpsLink(game.getId(), vpsMatch.getExtTableId(), vpsMatch.getExtTableVersionId());
    }
    return vpsMatch;
  }

  @NonNull
  public TableDetails autoFill(Game game, TableDetails tableDetails, boolean overwrite, boolean simulate) {
    String vpsTableId = game.getExtTableId();
    String vpsTableVersionId = game.getExtTableVersionId();
    TableInfo tableInfo = vpxService.getTableInfo(game);

    if (!StringUtils.isEmpty(vpsTableId)) {
      VpsTable vpsTable = vpsService.getTableById(vpsTableId);
      if (vpsTable != null) {
        if ((tableDetails.getGameYear() == null || tableDetails.getGameYear() == 0 || overwrite) && vpsTable.getYear() > 0) {
          tableDetails.setGameYear(vpsTable.getYear());
        }

        if ((tableDetails.getNumberOfPlayers() == null || tableDetails.getNumberOfPlayers() == 0 || overwrite) && vpsTable.getPlayers() > 0) {
          tableDetails.setNumberOfPlayers(vpsTable.getPlayers());
        }

        if ((overwrite || StringUtils.isEmpty(tableDetails.getUrl())) && !StringUtils.isEmpty(vpsTable.getIpdbUrl())) {
          tableDetails.setUrl(vpsTable.getIpdbUrl());

          String url = vpsTable.getIpdbUrl();
          if (url.contains("id=")) {
            tableDetails.setIPDBNum(url.substring(url.indexOf("id=") + 3));
          }
        }

        if ((overwrite || StringUtils.isEmpty(tableDetails.getGameTheme())) && vpsTable.getTheme() != null && !vpsTable.getTheme().isEmpty()) {
          tableDetails.setGameTheme(String.join(",", vpsTable.getTheme()));
        }

        if ((overwrite || StringUtils.isEmpty(tableDetails.getDesignedBy())) && vpsTable.getDesigners() != null && !vpsTable.getDesigners().isEmpty()) {
          tableDetails.setDesignedBy(String.join(",", vpsTable.getDesigners()));
        }

        if ((overwrite || StringUtils.isEmpty(tableDetails.getManufacturer())) && !StringUtils.isEmpty(vpsTable.getManufacturer())) {
          tableDetails.setManufacturer(vpsTable.getManufacturer());
        }

        if ((overwrite || tableDetails.getGameType() == null) && !StringUtils.isEmpty(vpsTable.getType())) {
          try {
            GameType gameType = GameType.valueOf(vpsTable.getType());
            tableDetails.setGameType(gameType);
          }
          catch (Exception e) {
            //ignore
          }
        }

        if (!StringUtils.isEmpty(vpsTableVersionId)) {
          VpsTableVersion tableVersion = vpsTable.getTableVersionById(vpsTableVersionId);
          if (tableVersion != null) {
            if ((overwrite || StringUtils.isEmpty(tableDetails.getGameVersion())) && !StringUtils.isEmpty(tableVersion.getVersion())) {
              tableDetails.setGameVersion(tableVersion.getVersion());
            }

            List<String> authors = tableVersion.getAuthors();
            if (overwrite || StringUtils.isEmpty(tableDetails.getAuthor())) {
              if (authors != null) {
                tableDetails.setAuthor(String.join(", ", authors));
              }
            }

            if (overwrite || (StringUtils.isEmpty(tableDetails.getgDetails()) && tableInfo != null && tableInfo.getTableDescription() != null)) {
              if (tableInfo != null) {
                StringBuilder details = new StringBuilder();
                if (!StringUtils.isEmpty(tableVersion.getComment())) {
                  details.append("VPS Comment:\n");
                  details.append(tableVersion.getComment());
                }
                String tableDescription = tableInfo.getTableDescription();
                details.append("\n\n");
                details.append(tableDescription);
                tableDetails.setgDetails(details.toString());
              }
            }

            if (overwrite || (StringUtils.isEmpty(tableDetails.getgNotes()) && tableInfo != null && tableInfo.getTableRules() != null)) {
              if (tableInfo != null) {
                tableDetails.setgNotes(tableInfo.getTableRules());
              }
            }

//            if (overwrite || StringUtils.isEmpty(tableDetails.getTags())) {
//              if (tableVersion.getFeatures() != null) {
//                String tags = String.join(", ", tableVersion.getFeatures());
//                String tableDetailTags = tableDetails.getTags() != null ? tableDetails.getTags() : "";
//                if (!tableDetailTags.contains(tags)) {
//                  tableDetailTags = tableDetailTags + ", " + tags;
//                }
//                tableDetails.setTags(tableDetailTags);
//              }
//            }
            LOG.info("Auto-applied VPS table version \"" + tableVersion + "\" (" + tableVersion.getId() + ")");
          }
        }
        else {
          fillTableInfoWithVpxData(tableInfo, game, tableDetails, overwrite);
        }
      }
    }
    else {
      fillTableInfoWithVpxData(tableInfo, game, tableDetails, overwrite);
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
    gameService.vpsLink(gameId, extTableId, extTableVersionId);

    // update the table in the frontend
    TableDetails tableDetails = getTableDetails(gameId);
    if (tableDetails != null) {
      tableDetails.setMappedValue(serverSettings.getMappingVpsTableId(), null);
      tableDetails.setMappedValue(serverSettings.getMappingVpsTableVersionId(), null);
      saveTableDetails(tableDetails, gameId, false);
    }
  }

  public void fixGameVersion(int gameId, String version) {
    TableDetails tableDetails = getTableDetails(gameId);
    if (tableDetails != null) {
      tableDetails.setGameVersion(version);
      saveTableDetails(tableDetails, gameId, false);
    }
  }

  /**
   * Some fallback: we use the VPX script metadata for popper if the VPS version data has not been applied.
   */
  private void fillTableInfoWithVpxData(TableInfo tableInfo, @NonNull Game game, @NonNull TableDetails tableDetails, boolean overwrite) {
    if (tableInfo != null) {
      if ((overwrite || StringUtils.isEmpty(tableDetails.getGameVersion())) && !StringUtils.isEmpty(tableInfo.getTableVersion())) {
        tableDetails.setGameVersion(tableInfo.getTableVersion());
      }

      if ((overwrite || StringUtils.isEmpty(tableDetails.getAuthor())) && !StringUtils.isEmpty(tableInfo.getAuthorName())) {
        tableDetails.setAuthor(tableInfo.getAuthorName());
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
    String existingRom = String.valueOf(oldDetails.getRomName());
    boolean romChanged = !String.valueOf(newDetails.getRomName()).equalsIgnoreCase(existingRom);

    String existingHsName = String.valueOf(oldDetails.getMappedValue(serverSettings.getMappingHsFileName()));
    boolean hsChanged = !String.valueOf(newDetails.getMappedValue(serverSettings.getMappingHsFileName())).equalsIgnoreCase(existingHsName);
    if (romChanged || hsChanged) {
      LOG.info("Game highscore data fields have been changed, triggering score check.");
      highscoreService.scanScore(game);
      cardService.generateCard(game);
    }
  }

  public boolean restart() {
    frontendService.restartFrontend();
    return true;
  }

  public void augmentWheel(Game game, String badge) {
    GameMediaItem gameMediaItem = game.getGameMedia().getDefaultMediaItem(VPinScreen.Wheel);
    if (gameMediaItem != null) {
      File wheelIcon = gameMediaItem.getFile();
      WheelAugmenter augmenter = new WheelAugmenter(wheelIcon);

      File badgeFile = systemService.getBadgeFile(badge);
      if (badgeFile.exists()) {
        augmenter.augment(badgeFile);
      }
    }
  }

  public void deAugmentWheel(Game game) {
    GameMediaItem gameMediaItem = game.getGameMedia().getDefaultMediaItem(VPinScreen.Wheel);
    if (gameMediaItem != null) {
      File wheelIcon = gameMediaItem.getFile();
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
        List<GameMediaItem> gameMediaItems = original.getGameMedia().getMediaItems(originalScreenValue);
        for (GameMediaItem gameMediaItem : gameMediaItems) {
          if (gameMediaItem.getFile().exists()) {
            File mediaFile = gameMediaItem.getFile();
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
      List<GameMediaItem> gameMediaItems = game.getGameMedia().getMediaItems(screen);
      for (GameMediaItem gameMediaItem : gameMediaItems) {
        File gameMediaFile = gameMediaItem.getFile();
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
    preferencesService.addChangeListener(this);
    preferenceChanged(PreferenceNames.SERVER_SETTINGS, null, null);
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) {
    if (propertyName.equals(PreferenceNames.SERVER_SETTINGS)) {
      serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    }
  }
}
