package de.mephisto.vpin.server;

import de.mephisto.vpin.server.b2s.B2SImageRatio;
import de.mephisto.vpin.server.b2s.DirectB2SManager;
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
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class VPinService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinService.class);

  private SqliteConnector sqliteConnector;

  private RomManager romManager;

  private HighscoreManager highscoreManager;

  private ExecutorService executor = Executors.newSingleThreadExecutor();

  private static VPinService instance;

  private DOFManager dofManager;

  private PopperManager popperManager;

  private DOFCommandData dofCommandData;

  private DirectB2SManager directB2SManager;

  private List<GameInfo> gameInfos = new ArrayList<>();

  public VPinService() {
    init(true);
  }

  private void init(boolean headless) {
    try {
      if (!SystemInfo.getInstance().getPinUPSystemFolder().exists()) {
        throw new FileNotFoundException("Wrong PinUP Popper installation folder: " + SystemInfo.getInstance().getPinUPSystemFolder().getAbsolutePath() + ".\nPlease fix the PinUP Popper installation path in file ./resources/env.properties");
      }
      if (!SystemInfo.getInstance().getVisualPinballInstallationFolder().exists()) {
        throw new FileNotFoundException("Wrong Visual Pinball installation folder: " + SystemInfo.getInstance().getVisualPinballInstallationFolder().getAbsolutePath() + ".\nPlease fix the Visual Pinball installation path in file ./resources/env.properties");
      }

      this.romManager = new RomManager();
      this.sqliteConnector = new SqliteConnector(romManager);
      this.highscoreManager = new HighscoreManager();
      this.directB2SManager = new DirectB2SManager();
      this.popperManager = new PopperManager(sqliteConnector, highscoreManager);

      dofCommandData = DOFCommandData.create();
      this.dofManager = new DOFManager(dofCommandData);

//      if (headless) {
//        if (!SystemInfo.isAvailable(HttpServer.PORT)) {
//          LOG.warn("VPinService already running, exiting.");
//          System.exit(0);
//        }
//
//        this.httpServer = new HttpServer(popperManager);
//        this.dofManager.startRuleEngine();
//      }


      new Thread() {
        public void run() {
          OverlayWindowFX.main(new String[]{});
          LOG.info("Overlay listener started.");
        }
      }.start();

      new ApplicationTray(this);
      LOG.info("Application tray created.");
      if (headless) {
        LOG.info("VPinService created [headless-mode]");
      }
      else {
        LOG.info("VPinService created [config-mode]");
      }

    } catch (Exception e) {
      LOG.error("VPin Service failed to start: " + e.getMessage(), e);
//      throw new VPinServiceException(e);
    }
  }

  public void restart() throws VPinServiceException {
    this.shutdown();
    this.init(true);
  }

  @SuppressWarnings("unused")
  public void shutdown() {
    gameInfos.clear();
    this.executor.shutdown();
//    this.httpServer.stop();
  }

  @SuppressWarnings("unused")
  @NonNull
  public File createDirectB2SImage(@NonNull GameInfo info, @NonNull B2SImageRatio ratio, int cropWidth) throws VPinServiceException {
    directB2SManager.generateB2SImage(info, ratio, cropWidth);
    return info.getDirectB2SImage();
  }

  @SuppressWarnings("unused")
  @Nullable
  public File extractDirectB2SBackgroundImage(@NonNull GameInfo info) throws VPinServiceException {
    return directB2SManager.extractDirectB2SBackgroundImage(info);
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
  public GameInfo getGameInfo(int id) {
    return sqliteConnector.getGame(this, id);
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
  public List<GameInfo> getActiveGameInfos() {
    List<Integer> gameIdsFromPlaylists = this.sqliteConnector.getGameIdsFromPlaylists();
    List<GameInfo> games = sqliteConnector.getGames(this);
    return games.stream().filter(g -> gameIdsFromPlaylists.contains(g.getId())).collect(Collectors.toList());
  }

  public List<GameInfo> getGameInfos() {
    if (this.gameInfos.isEmpty()) {
      LOG.info("Starting Game Scan...");
      this.gameInfos.addAll(sqliteConnector.getGames(this));
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
    List<GameInfo> games = sqliteConnector.getGames(this);
    for (GameInfo gameInfo : games) {
      if (gameInfo.getGameFile().getName().equals(filename)) {
        return gameInfo;
      }
    }
    return null;
  }

  @NonNull
  public List<GameInfo> getGamesWithEmptyRoms() {
    List<GameInfo> games = sqliteConnector.getGames(this);
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
    List<GameInfo> games = sqliteConnector.getGames(this);
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
    return this.sqliteConnector.getGameByName(this, table);
  }

  public GameInfo getGameByFile(File file) {
    return this.sqliteConnector.getGameByFilename(this, file.getName());
  }
}
