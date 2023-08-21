package de.mephisto.vpin.server;

import de.mephisto.vpin.restclient.ArchiveType;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameDetailsRepository;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.system.SystemService;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract public class AbstractVPinServerTest {
  public static final String EM_TABLE_NAME = "Baseball (1970).vpx";
  public static final String EM_B2S_NAME = "Baseball (1970).directb2s";
  public static final File EM_TABLE = new File("testsystem/vPinball/VisualPinball/Tables/" + EM_TABLE_NAME);

  public static final String VPREG_TABLE_NAME = "Jaws.vpx";
  public static final String VPREG_B2S_NAME = "Jaws.directb2s";
  public static final File VPREG_TABLE = new File("testsystem/vPinball/VisualPinball/Tables/" + VPREG_TABLE_NAME);

  public static final String NVRAM_TABLE_NAME = "Twister (1996).vpx";
  public static final String NVRAM_B2S_NAME = "Twister (1996).directb2s";
  public static final File NVRAM_TABLE = new File("testsystem/vPinball/VisualPinball/Tables/" + NVRAM_TABLE_NAME);

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

  public void setupSystem() {
    pinUPConnector.deleteGames();
    gameDetailsRepository.deleteAll();
    systemService.setArchiveType(ArchiveType.VPA);


    pinUPConnector.importGame(EM_TABLE);
    pinUPConnector.importGame(VPREG_TABLE);
    pinUPConnector.importGame(NVRAM_TABLE);

    List<Game> games = gameService.getGames();
  }

}
