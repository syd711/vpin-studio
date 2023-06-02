package de.mephisto.vpin.server.games;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GamesTest extends AbstractVPinServerTest {


  @Autowired
  private GameService gameService;

  @Test
  public void testGames() {
    assertFalse(gameService.getGames().isEmpty());
  }

  @Test
  public void testGame() {
    long start = System.currentTimeMillis();
//    assertNotNull(gameService.getGame(44)); //STLE
    assertNotNull(gameService.getGame(242)); //godzilla
    System.out.println("Duration: " + (System.currentTimeMillis() - start));
  }
}
