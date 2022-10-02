package de.mephisto.vpin.server;

import de.mephisto.vpin.server.dof.DOFCommand;
import de.mephisto.vpin.server.dof.DOFCommandData;
import de.mephisto.vpin.server.dof.DOFManager;
import de.mephisto.vpin.server.dof.Unit;
import de.mephisto.vpin.server.fx.overlay.OverlayWindowFX;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreManager;
import de.mephisto.vpin.server.popper.PopperManager;
import de.mephisto.vpin.server.popper.PopperScreen;
import de.mephisto.vpin.server.popper.TableStatusChangeListener;
import de.mephisto.vpin.server.roms.RomManager;
import de.mephisto.vpin.server.util.SqliteConnector;
import de.mephisto.vpin.server.util.SystemInfo;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VPinService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(VPinService.class);

  @Autowired
  private SqliteConnector sqliteConnector;

  @Autowired
  private RomManager romManager;

  @Autowired
  private HighscoreManager highscoreManager;

  @Autowired
  private DOFManager dofManager;

  @Autowired
  private PopperManager popperManager;

  @Autowired
  private DOFCommandData dofCommandData;

  @Autowired
  private SystemInfo systemInfo;

  private List<GameInfo> gameInfos = new ArrayList<>();

  public VPinService() {
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (!systemInfo.getPinUPSystemFolder().exists()) {
      throw new FileNotFoundException("Wrong PinUP Popper installation folder: " + systemInfo.getPinUPSystemFolder().getAbsolutePath() + ".\nPlease fix the PinUP Popper installation path in file ./resources/env.properties");
    }
    if (!systemInfo.getVisualPinballInstallationFolder().exists()) {
      throw new FileNotFoundException("Wrong Visual Pinball installation folder: " + systemInfo.getVisualPinballInstallationFolder().getAbsolutePath() + ".\nPlease fix the Visual Pinball installation path in file ./resources/env.properties");
    }

    new Thread(() -> {
      OverlayWindowFX.main(new String[]{});
      LOG.info("Overlay listener started.");
    }).start();

    new ApplicationTray(this);
    LOG.info("Application tray created.");
    LOG.info("VPinService created");
  }

  public void restart() throws VPinServiceException {
//    this.shutdown();
  }

  @SuppressWarnings("unused")
  @NonNull
  public String validateScreenConfiguration(@NonNull PopperScreen screen) {
    return popperManager.validateScreenConfiguration(screen);
  }

  @SuppressWarnings("unused")
  public void addTableStatusChangeListener(@NonNull TableStatusChangeListener listener) {
    this.popperManager.addTableStatusChangeListener(listener);
  }

  @SuppressWarnings("unused")
  public void removeTableStatusChangeListener(@NonNull TableStatusChangeListener listener) {
    this.popperManager.removeTableStatusChangeListener(listener);
  }

  @SuppressWarnings("unused")
  public void updateDOFCommand(@NonNull DOFCommand command) {
    this.dofCommandData.updateDOFCommand(command);
  }

  @SuppressWarnings("unused")
  public void addDOFCommand(@NonNull DOFCommand command) {
    this.dofCommandData.addDOFCommand(command);
  }

  @SuppressWarnings("unused")
  public void removeDOFCommand(@NonNull DOFCommand command) {
    this.dofCommandData.removeDOFCommand(command);
  }

  @SuppressWarnings("unused")
  @NonNull
  public List<DOFCommand> getDOFCommands() {
    return dofCommandData.getCommands();
  }

  @SuppressWarnings("unused")
  @NonNull
  public List<Unit> getUnits() {
    return dofManager.getUnits();
  }

  @SuppressWarnings("unused")
  public Unit getUnit(int id) {
    return dofManager.getUnit(id);
  }

  @SuppressWarnings("unused")
  public GameInfo getGameInfo(int id) {
    return sqliteConnector.getGame(id);
  }

  @SuppressWarnings("unused")
  public List<GameInfo> getActiveGameInfos() {
    List<Integer> gameIdsFromPlaylists = this.sqliteConnector.getGameIdsFromPlaylists();
    List<GameInfo> games = sqliteConnector.getGames();
    return games.stream().filter(g -> gameIdsFromPlaylists.contains(g.getId())).collect(Collectors.toList());
  }

  public List<GameInfo> getGameInfos() {
    if (this.gameInfos.isEmpty()) {
      LOG.info("Starting Game Scan...");
      this.gameInfos.addAll(sqliteConnector.getGames());
      LOG.info("Loading of all GameInfo finished, loaded " + this.gameInfos.size() + " games.");
    }
    return this.gameInfos;
  }

  @SuppressWarnings("unused")
  public void refreshGameInfos() {
    this.gameInfos.clear();
    LOG.info("Resetted game info list.");
  }

  @SuppressWarnings("unused")
  @Nullable
  public String rescanRom(GameInfo gameInfo) {
    return this.romManager.scanRom(gameInfo);
  }

  @SuppressWarnings("unused")
  @Nullable
  public GameInfo getGameByVpxFilename(@NonNull String filename) {
    List<GameInfo> games = sqliteConnector.getGames();
    for (GameInfo gameInfo : games) {
      if (gameInfo.getGameFile().getName().equals(filename)) {
        return gameInfo;
      }
    }
    return null;
  }

  @NonNull
  public List<GameInfo> getGamesWithEmptyRoms() {
    List<GameInfo> games = sqliteConnector.getGames();
    List<GameInfo> result = new ArrayList<>();
    for (GameInfo gameInfo : games) {
      if (StringUtils.isEmpty(gameInfo.getRom())) {
        result.add(gameInfo);
      }
    }
    return result;
  }

  @Nullable
  public GameInfo getGameByRom(@NonNull String romName) {
    List<GameInfo> games = sqliteConnector.getGames();
    for (GameInfo gameInfo : games) {
      if (gameInfo.getRom() != null && gameInfo.getRom().equals(romName)) {
        return gameInfo;
      }
    }
    return null;
  }

  @Nullable
  public Highscore getHighscore(GameInfo gameInfo) {
    return highscoreManager.getHighscore(gameInfo);
  }

  @SuppressWarnings("unused")
  public GameInfo getGameByName(String table) {
    return this.sqliteConnector.getGameByName(table);
  }

  public GameInfo getGameByFile(File file) {
    return this.sqliteConnector.getGameByFilename(file.getName());
  }
}
