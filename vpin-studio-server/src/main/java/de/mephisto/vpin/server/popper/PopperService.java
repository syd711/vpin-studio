package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.TableManagerSettings;
import de.mephisto.vpin.restclient.games.GameList;
import de.mephisto.vpin.restclient.games.GameListItem;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.popper.*;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.vpx.TableInfo;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vps.VpsService;
import de.mephisto.vpin.server.vps.VpsTableDataChangedListener;
import de.mephisto.vpin.server.vpx.VPXService;
import edu.umd.cs.findbugs.annotations.NonNull;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PopperService implements InitializingBean, VpsTableDataChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(PopperService.class);

  private final List<PopperStatusChangeListener> listeners = new ArrayList<>();

  @Autowired
  private SystemService systemService;

  @Autowired
  private GameService gameService;

  @Autowired
  private PinUPConnector pinUPConnector;

  @Autowired
  private VPXService vpxService;

  @Autowired
  private VpsService vpsService;

  @Autowired
  private PreferencesService preferencesService;

  public PinUPControl getPinUPControlFor(PopperScreen screen) {
    return pinUPConnector.getPinUPControlFor(screen);
  }

  public PinUPControls getPinUPControls() {
    return pinUPConnector.getControls();
  }

  @SuppressWarnings("unused")
  public void addPopperStatusChangeListener(PopperStatusChangeListener listener) {
    this.listeners.add(listener);
  }

  public GameList getImportTables() {
    List<GameEmulator> emulators = pinUPConnector.getGameEmulators();
    GameList list = new GameList();
    for (GameEmulator emulator : emulators) {
      File vpxTablesFolder = emulator.getTablesFolder();
      File[] files = vpxTablesFolder.listFiles((dir, name) -> name.endsWith(".vpx"));
      if (files != null) {
        List<Game> games = pinUPConnector.getGames();
        List<String> filesNames = games.stream().map(Game::getGameFileName).collect(Collectors.toList());
        for (File file : files) {
          if (!filesNames.contains(file.getName())) {
            GameListItem item = new GameListItem();
            item.setName(file.getName());
            item.setEmuId(emulator.getId());
            list.getItems().add(item);
          }
        }
      }
    }
    return list;
  }

  public JobExecutionResult importTables(GameList list) {
    List<GameListItem> items = list.getItems();
    int count = 0;
    for (GameListItem item : items) {
      GameEmulator emulator = pinUPConnector.getGameEmulator(item.getEmuId());
      File tableFile = new File(emulator.getTablesFolder(), item.getName());
      if (tableFile.exists()) {
        int result = importVPXGame(tableFile, true, -1, item.getEmuId());
        if (result > 0) {
          gameService.scanGame(result);
          count++;
        }
      }
    }

    return JobExecutionResultFactory.ok("Imported " + count + " tables", -1);
  }

  public int importVPXGame(File file, boolean importToPopper, int playListId, int emuId) {
    if (importToPopper) {
      int gameId = pinUPConnector.importGame(file, emuId);
      if (gameId >= 0 && playListId >= 0) {
        pinUPConnector.addToPlaylist(playListId, gameId);
      }
      return gameId;
    }
    return -1;
  }

  public void notifyTableStatusChange(final Game game, final boolean started) {
    TableStatusChangedEvent event = () -> game;
    for (PopperStatusChangeListener listener : this.listeners) {
      if (started) {
        listener.tableLaunched(event);
      }
      else {
        listener.tableExited(event);
      }
    }
  }

  public boolean isPinUPRunning() {
    Optional<ProcessHandle> pinUP = ProcessHandle.allProcesses().filter(p -> p.info().command().isPresent() && p.info().command().get().contains("PinUP")).findFirst();
    Optional<ProcessHandle> vpx = ProcessHandle.allProcesses().filter(p -> p.info().command().isPresent() && p.info().command().get().contains("VPinball")).findFirst();
    return pinUP.isPresent() || vpx.isPresent();
  }

  public boolean terminate() {
    boolean b = systemService.killPopper();
    for (PopperStatusChangeListener listener : listeners) {
      listener.popperExited();
    }
    return b;
  }

  public void notifyPopperLaunch() {
    for (PopperStatusChangeListener listener : listeners) {
      listener.popperLaunched();
    }
  }

  public void notifyPopperRestart() {
    for (PopperStatusChangeListener listener : listeners) {
      listener.popperRestarted();
    }
  }

  public void notifyPopperExit() {
    for (PopperStatusChangeListener listener : listeners) {
      listener.popperExited();
    }
  }

  @NonNull
  public TableDetails autofillTableDetails(Game game, boolean overwrite) {
    TableDetails tableDetails = pinUPConnector.getTableDetails(game.getId());
    if (!StringUtils.isEmpty(game.getExtTableId())) {
      VpsTable vpsTable = VPS.getInstance().getTableById(game.getExtTableId());
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
          } catch (Exception e) {
            //ignore
          }
        }

        if (!StringUtils.isEmpty(game.getExtTableVersionId())) {
          VpsTableVersion tableVersion = vpsTable.getVersion(game.getExtTableVersionId());
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

            if (overwrite || StringUtils.isEmpty(tableDetails.getNotes())) {
              tableDetails.setNotes(tableVersion.getComment());
            }

            if (overwrite || StringUtils.isEmpty(tableDetails.getTags())) {
              if (tableVersion.getFeatures() != null) {
                tableDetails.setTags(String.join(", ", tableVersion.getFeatures()));
              }
            }
          }
        }
        else {
          fillTableInfoWithVpxData(game, tableDetails, overwrite);
        }
      }
    }
    else {
      fillTableInfoWithVpxData(game, tableDetails, overwrite);
    }

    pinUPConnector.saveTableDetails(game.getId(), tableDetails);
    LOG.info("Finished auto-fill for \"" + game.getGameDisplayName() + "\"");
    return tableDetails;
  }

  /**
   * Some fallback: we use the VPX script metadata for popper if the VPS version data has not been applied.
   */
  private void fillTableInfoWithVpxData(@NonNull Game game, @NonNull TableDetails tableDetails, boolean overwrite) {
    TableInfo tableInfo = vpxService.getTableInfo(game);
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
    return pinUPConnector.getTableDetails(gameId);
  }

  public TableDetails saveTableDetails(TableDetails updatedTableDetails, int gameId) {
    TableDetails oldDetails = getTableDetails(gameId);
    String gameFilename = updatedTableDetails.getGameFileName();
    if (!gameFilename.endsWith(".vpx")) {
      gameFilename = gameFilename + ".vpx";
      updatedTableDetails.setGameFileName(gameFilename);
    }

    pinUPConnector.saveTableDetails(gameId, updatedTableDetails);

    Game game = pinUPConnector.getGame(gameId);
    try {
      if (!String.valueOf(game.getRom()).equals(String.valueOf(updatedTableDetails.getRomName())) && !StringUtils.isEmpty(updatedTableDetails.getRomName())) {
        game.setRom(updatedTableDetails.getRomName());
        gameService.save(game);
      }

      if (!String.valueOf(game.getTableName()).equals(String.valueOf(updatedTableDetails.getRomAlt())) && !StringUtils.isEmpty(updatedTableDetails.getRomAlt())) {
        game.setTableName(updatedTableDetails.getRomAlt());
        gameService.save(game);
      }
    } catch (Exception e) {
      LOG.error("Error updating table for table details: " + e.getMessage());
    }
    if (!updatedTableDetails.getGameFileName().equals(oldDetails.getGameFileName())) {
      String name = FilenameUtils.getBaseName(updatedTableDetails.getGameFileName());
      de.mephisto.vpin.commons.utils.FileUtils.rename(game.getGameFile(), name);
      de.mephisto.vpin.commons.utils.FileUtils.rename(game.getDirectB2SFile(), name);
      de.mephisto.vpin.commons.utils.FileUtils.rename(game.getPOVFile(), name);
      de.mephisto.vpin.commons.utils.FileUtils.rename(game.getResFile(), name);
      LOG.info("Finished game file renaming from \"" + oldDetails.getGameFileName() + "\" to \"" + game.getGameFileName() + "\"");
    }

    return updatedTableDetails;
  }

  public boolean restart() {
    systemService.restartPopper();
    return true;
  }

  public void augmentWheel(Game game, String badge) {
    GameMediaItem gameMediaItem = game.getGameMedia().getDefaultMediaItem(PopperScreen.Wheel);
    if (gameMediaItem != null) {
      File wheelIcon = gameMediaItem.getFile();
      WheelAugmenter augmenter = new WheelAugmenter(wheelIcon);

      File badgeFile = systemService.getBagdeFile(badge);
      if (badgeFile.exists()) {
        augmenter.augment(badgeFile);
      }
    }
  }

  public void deAugmentWheel(Game game) {
    GameMediaItem gameMediaItem = game.getGameMedia().getDefaultMediaItem(PopperScreen.Wheel);
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
    Game game = pinUPConnector.getGameByFilename(file.getAbsolutePath());
    if (game != null) {
      descriptor.setPlaylistId(pinUPConnector.getPlayListForGame(game.getId()).getId());
    }
    return descriptor;
  }

  public void cloneGameMedia(Game original, Game clone) {
    PopperScreen[] values = PopperScreen.values();
    for (PopperScreen originalScreenValue : values) {
      try {
        List<GameMediaItem> gameMediaItems = original.getGameMedia().getMediaItems(originalScreenValue);
        for (GameMediaItem gameMediaItem : gameMediaItems) {
          if (gameMediaItem.getFile().exists()) {
            File mediaFile = gameMediaItem.getFile();
            String suffix = FilenameUtils.getExtension(mediaFile.getName());
            File cloneTarget = new File(clone.getPinUPMediaFolder(originalScreenValue), clone.getGameName() + "." + suffix);
            if (mediaFile.getName().equals(cloneTarget.getName())) {
              LOG.warn("Source name and target name of media asset " + mediaFile.getAbsolutePath() + " are identical, skipping cloning.");
              return;
            }

            if (cloneTarget.exists() && !cloneTarget.delete()) {
              LOG.error("Failed to clone media asset " + cloneTarget.getAbsolutePath() + ": deletion of existing asset failed.");
              return;
            }
            FileUtils.copyFile(mediaFile, cloneTarget);
            LOG.info("Cloned PinUP Popper media: " + mediaFile.getAbsolutePath() + " to " + cloneTarget.getAbsolutePath());
          }
        }
      } catch (IOException e) {
        LOG.info("Failed to clone popper media: " + e.getMessage(), e);
      }
    }
  }

  public void renameGameMedia(Game game, String oldBaseName, String newBaseName) {
    PopperScreen[] values = PopperScreen.values();
    int assetRenameCounter = 0;
    for (PopperScreen screen : values) {
      List<GameMediaItem> gameMediaItems = game.getGameMedia().getMediaItems(screen);
      for (GameMediaItem gameMediaItem : gameMediaItems) {
        File gameMediaFile = gameMediaItem.getFile();
        if (gameMediaFile.exists()) {
          if (de.mephisto.vpin.commons.utils.FileUtils.rename(gameMediaFile, newBaseName)) {
            assetRenameCounter++;
            LOG.info("[" + screen + "] Renamed PinUP Popper media from \"" + gameMediaFile.getName() + "\" to name \"" + newBaseName + "\"");
          }
          else {
            LOG.warn("[" + screen + "] Renaming PinUP Popper media from \"" + gameMediaFile.getName() + "\" to name \"" + newBaseName + "\" failed.");
          }
        }
      }
    }
    LOG.info("Finished asset renaming for \"" + oldBaseName + "\" to \"" + newBaseName + "\", renamed " + assetRenameCounter + " assets.");
  }

  public PopperCustomOptions getCustomOptions() {
    return pinUPConnector.getCustomOptions();
  }

  public PopperCustomOptions saveCustomOptions(PopperCustomOptions options) {
    pinUPConnector.updateCustomOptions(options);
    return options;
  }

  public List<GameEmulator> getGameEmulators() {
    return pinUPConnector.getGameEmulators();
  }

  public List<PinUPPlayerDisplay> getPupPlayerDisplays() {
    return pinUPConnector.getPupPlayerDisplays();
  }

  public List<GameEmulator> getBackglassGameEmulators() {
    return pinUPConnector.getBackglassGameEmulators();
  }

  public GameEmulator getGameEmulator(int id) {
    return pinUPConnector.getGameEmulator(id);
  }

  @Override
  public void tableDataChanged(@NotNull Game game) {
    try {
      ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
      boolean autoApply = serverSettings.isVpsAutoApplyToPopper();
      autofillTableDetails(game, autoApply);
    } catch (Exception e) {
      LOG.error("Failed to execute auto-filling of game details: " + e.getMessage(), e);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Thread shutdownHook = new Thread(this::notifyPopperExit);
    Runtime.getRuntime().addShutdownHook(shutdownHook);
    vpsService.addVpsTableDataChangeListener(this);
  }
}
