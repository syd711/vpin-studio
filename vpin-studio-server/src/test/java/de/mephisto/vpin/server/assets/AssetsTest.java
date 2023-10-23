package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.players.Player;
import org.apache.commons.io.IOUtils;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class AssetsTest extends AbstractVPinServerTest {

  @Test
  public void testFinishMessage() throws Exception {
    super.clearVPinStudioDatabase();

    Competition competition = super.createOfflineCompetition(AbstractVPinServerTest.VPREG_TABLE_NAME);
    competition.setWinnerInitials("FFF");
    Game game = gameService.getGame(competition.getGameId());
    ScoreSummary scoreSummary = highscoreService.getScoreSummary(0, game, null);

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
