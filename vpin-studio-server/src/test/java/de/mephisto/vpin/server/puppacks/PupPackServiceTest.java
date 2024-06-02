package de.mephisto.vpin.server.puppacks;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.puppack.PupPack;
import de.mephisto.vpin.server.puppack.PupPacksService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class PupPackServiceTest extends AbstractVPinServerTest {


  @Autowired
  protected PupPacksService pupPacksService;

  @Test
  public void testPupPacks() {
    List<Game> knownGames = gameService.getKnownGames(-1);
    for (Game knownGame : knownGames) {
      PupPack pupPack = pupPacksService.getPupPack(knownGame);
      if (pupPack != null) {
        System.out.println(pupPack.getPupPackFolder().getAbsolutePath());
      }
      else {
        System.out.println("No pup pack for " + knownGame.getGameDisplayName());
      }
    }
  }
}
