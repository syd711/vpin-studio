package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.players.Player;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class AssetsTest extends AbstractVPinServerTest {

  @Test
  public void testFinishMessage() throws Exception {
    super.clearVPinStudioDatabase();

    Competition competition = super.createOfflineCompetition(AbstractVPinServerTest.VPREG_TABLE_NAME);
    competition.setWinnerInitials("FFF");
    Game game = gameService.getGame(competition.getGameId());
    ScoreSummary scoreSummary = highscoreService.getScoreSummary(0, game);

    Player player = new Player();
    player.setInitials("FFF");
    player.setName("Matthias [MFA]");

    byte[] competitionFinishedCard = assetService.getCompetitionFinishedCard(competition, game, player, scoreSummary);
    assertTrue(competitionFinishedCard.length > 0);
//    File out = new File("E:\\temp\\out.png");
//    if(out.exists()) {
//      out.delete();
//    }
//    IOUtils.write(competitionFinishedCard, new FileOutputStream(out));
  }

}
