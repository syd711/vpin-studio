package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.restclient.PinUPControl;
import de.mephisto.vpin.server.VPinServerTest;
import de.mephisto.vpin.server.games.Game;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PinUPConnectorTest extends VPinServerTest {

  @Autowired
  private PinUPConnector connector;

  @Test
  public void testControls() {
    List<PinUPControl> controls = connector.getControls();
    assertFalse(controls.isEmpty());
  }

  @Test
  public void testConnector() {
    List<Game> games = connector.getGames();
    assertFalse(games.isEmpty());

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

//  @Test
//  public void testWheelAugmentation() {
//    File wheelIcon = new File("C:\\vPinball\\PinUPSystem\\POPMedia\\Visual Pinball X\\Wheel\\Aliens 2.0.png");
//    File badge = new File("E:\\Development\\workspace\\vpin-studio\\resources\\competition-badges\\trophy-1.png");
//
//    WheelAugmenter augmenter = new WheelAugmenter(wheelIcon);
//    augmenter.deAugment();
//    augmenter.augment(badge);
//  }

}
