package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.restclient.PinUPControl;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.games.Game;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PinUPConnectorTest extends AbstractVPinServerTest {

  @Autowired
  private PinUPConnector connector;


  @Test
  public void testPlaylists() {
    List<Playlist> playLists = connector.getPlayLists();
    assertFalse(playLists.isEmpty());
  }

  @Test
  public void testGameAdding() {
    File file = new File("src/test/resources/Aces High (1965).vpx");
    int l = connector.importGame(file);
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
    assertNotNull(connector.getEmulatorExitScript(Emulator.VISUAL_PINBALL_X));
    assertNotNull(connector.getEmulatorExitScript(Emulator.FUTURE_PINBALL));
    assertNotNull(connector.getEmulatorStartupScript(Emulator.VISUAL_PINBALL_X));
    assertNotNull(connector.getEmulatorStartupScript(Emulator.FUTURE_PINBALL));

    Game volgame = connector.getGame(games.get(0).getId());
    int volume = volgame.getVolume();

    connector.updateVolume(game, volume);
    volgame = connector.getGame(games.get(0).getId());
    assertEquals(volume, volgame.getVolume());

    volume = volume + 1;
    connector.updateVolume(game, volume);
    volgame = connector.getGame(games.get(0).getId());
    assertEquals(volume, volgame.getVolume());
  }

}
