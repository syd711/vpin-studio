package de.mephisto.vpin.server;

import de.mephisto.vpin.restclient.backups.BackupType;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.server.backups.BackupService;
import de.mephisto.vpin.server.backups.adapters.TableBackupAdapterFactory;
import de.mephisto.vpin.server.assets.AssetRepository;
import de.mephisto.vpin.server.assets.AssetService;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.CompetitionsRepository;
import de.mephisto.vpin.server.frontend.FrontendResource;
import de.mephisto.vpin.server.games.*;
import de.mephisto.vpin.server.highscores.parsing.HighscoreParsingService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.players.PlayerRepository;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.FrontendStatusEventsResource;

import de.mephisto.vpin.server.system.SystemService;
import org.jcodec.common.logging.Logger;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import static de.mephisto.vpin.server.VPinStudioServer.Features;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract public class AbstractVPinServerTest {
  public static final String EM_TABLE_NAME = "Baseball (1970).vpx";
  public static final String EM_ROM_NAME = "Baseball_1970";
  public static final String EM_B2S_NAME = "Baseball (1970).directb2s";
  public static final File EM_TABLE = new File("../testsystem/vPinball/VisualPinball/Tables/" + EM_TABLE_NAME);

  public static final String VPREG_TABLE_NAME = "Jaws.vpx";
  public static final String VPREG_ROM_NAME = "Jaws";
  public static final String VPREG_B2S_NAME = "Jaws.directb2s";
  public static final File VPREG_TABLE = new File("../testsystem/vPinball/VisualPinball/Tables/" + VPREG_TABLE_NAME);

  public static final String NVRAM_TABLE_NAME = "Twister (1996).vpx";
  public static final String NVRAM_B2S_NAME = "Twister (1996).directb2s";
  public static final String NVRAM_ROM_NAME = "twst_405";
  public static final File NVRAM_TABLE = new File("../testsystem/vPinball/VisualPinball/Tables/" + NVRAM_TABLE_NAME);

  public static final List<String> ROM = Arrays.asList(EM_ROM_NAME, VPREG_ROM_NAME, NVRAM_ROM_NAME);
  public static final List<String> TABLE_NAMES = Arrays.asList(EM_TABLE_NAME, VPREG_TABLE_NAME, NVRAM_TABLE_NAME);

  static {
    SystemService.RESOURCES = "../resources/";
    Features.MANIA_ENABLED = false;
  }

  @Autowired
  protected FrontendService frontendService;

  @Autowired
  protected GameService gameService;

  @Autowired
  private GameDetailsRepositoryService gameDetailsRepositoryService;

  @Autowired
  protected SystemService systemService;

  @Autowired
  protected CompetitionService competitionService;

  @Autowired
  protected CompetitionsRepository competitionsRepository;

  @Autowired
  protected PlayerRepository playerRepository;

  @Autowired
  protected AssetRepository assetRepository;

  @Autowired
  protected AssetService assetService;

  @Autowired
  protected HighscoreService highscoreService;

  @Autowired
  protected FrontendStatusEventsResource frontendStatusEventsResource;

  @Autowired
  protected HighscoreParsingService highscoreParsingService;

  @Autowired
  protected FrontendResource frontendResource;

  @Autowired
  protected TableBackupAdapterFactory tableBackupAdapterFactory;

  @Autowired
  protected BackupService backupService;

  /**
   * To force usage of a given Frontend
   */
  protected GameEmulator buildGameEmulator() {
    GameEmulator emulator = new GameEmulator();
    emulator.setDescription("VPX");
    emulator.setType(EmulatorType.VisualPinball);
    emulator.setSafeName("VPX");
    emulator.setName("VPX");
    emulator.setInstallationDirectory("../testsystem/vPinball/VisualPinball/");
    emulator.setMameDirectory("../testsystem/vPinball/VisualPinball/VPinMAME/");
    emulator.setRomDirectory("../testsystem/vPinball/VisualPinball/VPinMAME/roms/");
    emulator.setNvramDirectory("../testsystem/vPinball/VisualPinball/VPinMAME/nvram/");
    emulator.setCfgDirectory("../testsystem/vPinball/VisualPinball/VPinMAME/cfg/");
    emulator.setMediaDirectory("../testsystem/vPinball/PinUPSystem/POPMedia");
    emulator.setGamesDirectory("../testsystem/vPinball/VisualPinball/Tables/");
    emulator.setGameExt("vpx");

    return emulator;
  }

  public void setupSystem(FrontendType frontendType) {
    systemService.setFrontendType(frontendType);
    // notify frontendService 
    frontendService.afterPropertiesSet();
    setupSystem();
  }

  public void setupSystem() {
    try {
      frontendService.deleteGames(1);
      clearVPinStudioDatabase();

      systemService.setBackupType(BackupType.VPA);

      frontendService.importGame(EM_TABLE, 1);
      frontendService.importGame(VPREG_TABLE, 1);
      frontendService.importGame(NVRAM_TABLE, 1);
    }
    catch (Exception e) {
      Logger.error("Failed to setup test system: {}", e.getMessage(), e);
    }
  }

  protected void clearVPinStudioDatabase() {
    gameDetailsRepositoryService.deleteAll();
    competitionsRepository.deleteAll();
    assetRepository.deleteAll();
    playerRepository.deleteAll();
  }

  protected Competition createOfflineCompetition(String filename) {
    int emuId = 1;
    Game game = gameService.getGameByFilename(emuId, filename);

    Competition competition = new Competition();
    competition.setGameId(game.getId());
    competition.setType(CompetitionType.OFFLINE.name());
    competition.setName(String.valueOf(new Date().getTime()));
    competition.setStartDate(new Date());
    competition.setEndDate(new Date());

    return competitionService.save(competition);
  }

}
