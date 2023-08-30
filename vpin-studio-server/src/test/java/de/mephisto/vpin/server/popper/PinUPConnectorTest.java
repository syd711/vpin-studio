package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.restclient.popper.Emulator;
import de.mephisto.vpin.restclient.popper.EmulatorType;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.games.Game;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PinUPConnectorTest extends AbstractVPinServerTest {

  @Autowired
  private PinUPConnector connector;

  @Test
  public void testGameAdding() {
    int l = connector.importGame(EM_TABLE);
    if (l > 0) {
      assertTrue(connector.deleteGame(l));
    }
    assertTrue(l > 0);
  }

  @Test
  public void testConnector() {
    List<Game> games = connector.getGames();
    assertFalse(games.isEmpty());

    List<Integer> gameIds = connector.getGameIds();
    assertFalse(gameIds.isEmpty());

    Game game = connector.getGame(games.get(0).getId());
    assertNotNull(game);

    Game gameByFilename = connector.getGameByFilename(game.getGameFile().getName());
    assertNotNull(gameByFilename);

    assertNotEquals(connector.getGameCount(), 0);

    assertNotNull(connector.getStartupScript());
    assertNotNull(connector.getEmulatorExitScript(EmulatorType.VISUAL_PINBALL_X));
    assertNotNull(connector.getEmulatorExitScript(EmulatorType.FUTURE_PINBALL));
    assertNotNull(connector.getEmulatorStartupScript(EmulatorType.VISUAL_PINBALL_X));
    assertNotNull(connector.getEmulatorStartupScript(EmulatorType.FUTURE_PINBALL));
  }

}
