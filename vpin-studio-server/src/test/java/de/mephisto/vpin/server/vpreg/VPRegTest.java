package de.mephisto.vpin.server.vpreg;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreMetadata;
import de.mephisto.vpin.server.highscores.VPReg;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class VPRegTest extends AbstractVPinServerTest {

  @Autowired
  private GameService gameService;

  @Test
  public void readFile() {
    File file = new File("C:\\vPinball\\VisualPinball\\User\\VPReg.stg");
    Game game = gameService.getGameByFilename("Stranger Things.vpx");

    VPReg reg = new VPReg(file, game);
    reg.resetHighscores();
    String s = reg.readHighscores();

    System.out.println(s);
    assertNotNull(s);
  }
}
