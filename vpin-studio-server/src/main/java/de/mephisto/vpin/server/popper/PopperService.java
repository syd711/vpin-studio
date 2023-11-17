package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableFile;
import de.mephisto.vpin.restclient.TableManagerSettings;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.popper.*;
import de.mephisto.vpin.restclient.tables.GameList;
import de.mephisto.vpin.restclient.tables.GameListItem;
import de.mephisto.vpin.restclient.vpx.TableInfo;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpx.VPXService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PopperService implements InitializingBean {
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
    return pinUP.isPresent();
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
      VpsTable tableData = VPS.getInstance().getTableById(game.getExtTableId());
      if (tableData != null) {
        if ((tableDetails.getGameYear() == null || tableDetails.getGameYear() == 0 || overwrite) && tableData.getYear() > 0) {
          tableDetails.setGameYear(tableData.getYear());
        }

        if ((tableDetails.getNumberOfPlayers() == null || tableDetails.getNumberOfPlayers() == 0 || overwrite) && tableData.getPlayers() > 0) {
          tableDetails.setNumberOfPlayers(tableData.getPlayers());
        }

        if ((overwrite || StringUtils.isEmpty(tableDetails.getUrl())) && !StringUtils.isEmpty(tableData.getIpdbUrl())) {
          tableDetails.setUrl(tableData.getIpdbUrl());

          String url = tableData.getIpdbUrl();
          if (url.contains("id=")) {
            tableDetails.setIPDBNum(url.substring(url.indexOf("id=") + 3));
          }
        }

        if ((overwrite || StringUtils.isEmpty(tableDetails.getGameTheme())) && tableData.getTheme() != null && !tableData.getTheme().isEmpty()) {
          tableDetails.setGameTheme(String.join(",", tableData.getTheme()));
        }

        if ((overwrite || StringUtils.isEmpty(tableDetails.getDesignedBy())) && tableData.getDesigners() != null && !tableData.getDesigners().isEmpty()) {
          tableDetails.setDesignedBy(String.join(",", tableData.getDesigners()));
        }

        if ((overwrite || StringUtils.isEmpty(tableDetails.getManufacturer())) && !StringUtils.isEmpty(tableData.getManufacturer())) {
          tableDetails.setManufacturer(tableData.getManufacturer());
        }

        if ((overwrite || tableDetails.getGameType() == null) && !StringUtils.isEmpty(tableData.getType())) {
          try {
            GameType gameType = GameType.valueOf(tableData.getType());
            tableDetails.setGameType(gameType);
          } catch (Exception e) {
            //ignore
          }
        }

        if (!StringUtils.isEmpty(game.getExtTableVersionId())) {
          List<VpsTableFile> tableFiles = tableData.getTableFiles();
          Optional<VpsTableFile> tableVersion = tableFiles.stream().filter(t -> t.getId().equals(game.getExtTableVersionId())).findFirst();
          if (tableVersion.isPresent()) {
            VpsTableFile version = tableVersion.get();
            if ((overwrite || StringUtils.isEmpty(tableDetails.getGameVersion())) && !StringUtils.isEmpty(version.getVersion())) {
              tableDetails.setGameVersion(version.getVersion());
            }
          }
        }

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
    }
    else {
      TableInfo tableInfo = vpxService.getTableInfo(game);
      if (tableInfo != null) {
        if (overwrite || StringUtils.isEmpty(tableDetails.getGameVersion())) {
          tableDetails.setGameVersion(tableInfo.getTableVersion());
        }
        if (overwrite || StringUtils.isEmpty(tableDetails.getAuthor())) {
          tableDetails.setAuthor(tableInfo.getAuthorName());
        }
      }
    }

    pinUPConnector.saveTableDetails(game.getId(), tableDetails);

    return tableDetails;
  }


  public TableDetails getTableDetails(int gameId) {
    return pinUPConnector.getTableDetails(gameId);
  }

  public TableDetails saveTableDetails(TableDetails updatedTableDetails, int gameId) {
    Game game = pinUPConnector.getGame(gameId);
    pinUPConnector.saveTableDetails(game.getId(), updatedTableDetails);

    String gameFilename = game.getGameFileName();
    if (!gameFilename.endsWith(".vpx")) {
      gameFilename = gameFilename + ".vpx";
    }

    if (!updatedTableDetails.getGameFileName().equals(gameFilename)) {
      String name = FilenameUtils.getBaseName(updatedTableDetails.getGameFileName());
      de.mephisto.vpin.commons.utils.FileUtils.rename(game.getGameFile(), name);
      de.mephisto.vpin.commons.utils.FileUtils.rename(game.getDirectB2SFile(), name);
      de.mephisto.vpin.commons.utils.FileUtils.rename(game.getPOVFile(), name);
      de.mephisto.vpin.commons.utils.FileUtils.rename(game.getResFile(), name);
      LOG.info("Finished game file renaming to " + game.getGameFileName());
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

  //TODO use or delete?
  public void renameGameMedia(Game game, String oldName, String newBaseName) {
    PopperScreen[] values = PopperScreen.values();
    for (PopperScreen originalScreenValue : values) {

      List<GameMediaItem> gameMediaItems = game.getGameMedia().getMediaItems(originalScreenValue);
      for (GameMediaItem gameMediaItem : gameMediaItems) {
        File gameMediaFile = gameMediaItem.getFile();
        if (gameMediaFile.exists()) {
          String name = gameMediaFile.getName();
          String newName = name.replace(oldName, newBaseName);
          File target = new File(gameMediaFile.getParentFile(), newName);
          if (gameMediaFile.renameTo(target)) {
            LOG.info("Renamed PinUP Popper media from " + gameMediaFile.getAbsolutePath() + " to " + target.getAbsolutePath());
          }
          else {
            LOG.warn("Renaming PinUP Popper media from " + gameMediaFile.getAbsolutePath() + " to " + target.getName() + " failed.");
          }
        }
      }
    }
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

  public List<GameEmulator> getBackglassGameEmulators() {
    return pinUPConnector.getBackglassGameEmulators();
  }

  public GameEmulator getGameEmulator(int id) {
    return pinUPConnector.getGameEmulator(id);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Thread shutdownHook = new Thread(this::notifyPopperExit);
    Runtime.getRuntime().addShutdownHook(shutdownHook);
  }
}
