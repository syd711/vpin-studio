package de.mephisto.vpin.server;

import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameDetailsRepository;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.system.SystemService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract public class AbstractVPinServerTest {
  public static final File EM_TABLE = new File("testsystem/vPinball/VisualPinball/Tables/Baseball (1970).vpx");
  public static final File VPREG_TABLE = new File("testsystem/vPinball/VisualPinball/Tables/Jaws.vpx");
  public static final File NVRAM_TABLE = new File("testsystem/vPinball/VisualPinball/Tables/Twister (1996).vpx");

  static {
    SystemService.RESOURCES = "../resources/";
  }

  @Autowired
  protected PinUPConnector pinUPConnector;

  @Autowired
  protected GameService gameService;

  @Autowired
  protected GameDetailsRepository gameDetailsRepository;

  @BeforeAll
  public void setupSystem() {
    pinUPConnector.deleteGames();
    gameDetailsRepository.deleteAll();

    pinUPConnector.importGame(EM_TABLE);
    pinUPConnector.importGame(VPREG_TABLE);
    pinUPConnector.importGame(NVRAM_TABLE);

    List<Game> games = gameService.getGames();
  }

}
