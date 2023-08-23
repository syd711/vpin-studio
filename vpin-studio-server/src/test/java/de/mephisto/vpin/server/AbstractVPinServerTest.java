package de.mephisto.vpin.server;

import de.mephisto.vpin.restclient.ArchiveType;
import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.server.assets.AssetRepository;
import de.mephisto.vpin.server.assets.AssetService;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.CompetitionsRepository;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameDetailsRepository;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreParser;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.players.PlayerRepository;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.popper.PopperResource;
import de.mephisto.vpin.server.popper.PopperServiceResource;
import de.mephisto.vpin.server.system.SystemService;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract public class AbstractVPinServerTest {
  public static final String EM_TABLE_NAME = "Baseball (1970).vpx";
  public static final String EM_ROM_NAME = "Baseball_1970";
  public static final String EM_B2S_NAME = "Baseball (1970).directb2s";
  public static final File EM_TABLE = new File("testsystem/vPinball/VisualPinball/Tables/" + EM_TABLE_NAME);

  public static final String VPREG_TABLE_NAME = "Jaws.vpx";
  public static final String VPREG_ROM_NAME = "Jaws";
  public static final String VPREG_B2S_NAME = "Jaws.directb2s";
  public static final File VPREG_TABLE = new File("testsystem/vPinball/VisualPinball/Tables/" + VPREG_TABLE_NAME);

  public static final String NVRAM_TABLE_NAME = "Twister (1996).vpx";
  public static final String NVRAM_B2S_NAME = "Twister (1996).directb2s";
  public static final String NVRAM_ROM_NAME = "twst_405";
  public static final File NVRAM_TABLE = new File("testsystem/vPinball/VisualPinball/Tables/" + NVRAM_TABLE_NAME);

  public static final List<String> ROM = Arrays.asList(EM_ROM_NAME, VPREG_ROM_NAME, NVRAM_ROM_NAME);

  static {
    SystemService.RESOURCES = "../resources/";
  }

  @Autowired
  protected PinUPConnector pinUPConnector;

  @Autowired
  protected GameService gameService;

  @Autowired
  protected GameDetailsRepository gameDetailsRepository;

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
  protected PopperResource popperResource;

  @Autowired
  protected HighscoreParser highscoreParser;

  @Autowired
  protected PopperServiceResource popperServiceResource;

  public void setupSystem() {
    pinUPConnector.deleteGames();

    clearVPinStudioDatabase();

    systemService.setArchiveType(ArchiveType.VPA);


    pinUPConnector.importGame(EM_TABLE);
    pinUPConnector.importGame(VPREG_TABLE);
    pinUPConnector.importGame(NVRAM_TABLE);

    List<Game> games = gameService.getGames();
  }

  protected void clearVPinStudioDatabase() {
    gameDetailsRepository.deleteAll();
    competitionsRepository.deleteAll();
    assetRepository.deleteAll();
    playerRepository.deleteAll();
  }

  protected Competition createOfflineCompetition(String filename) {
    Game game = gameService.getGameByFilename(filename);

    Competition competition = new Competition();
    competition.setGameId(game.getId());
    competition.setType(CompetitionType.OFFLINE.name());
    competition.setName(String.valueOf(new Date().getTime()));
    competition.setStartDate(new Date());
    competition.setEndDate(new Date());

    return competitionService.save(competition);
  }

}
