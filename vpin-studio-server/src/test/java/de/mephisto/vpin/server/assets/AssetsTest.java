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
    Competition competition = competitionService.getCompetition(1661);
    competition.setWinnerInitials("FFF");
    Game game = gameService.getGame(competition.getGameId());
    ScoreSummary scoreSummary = highscoreService.getScoreSummary(0, competition.getGameId(), null);

    Player player = new Player();
    player.setInitials("FFF");
//    player.setAvatarUrl("https://cdn.discordapp.com/avatars/1095393211545042974/53e91c60c464fad3e596496477ddcca9.png");
    player.setName("Matthias [MFA]");
    player.setAvatar(assetService.getById(1193));


    byte[] competitionFinishedCard = assetService.getCompetitionFinishedCard(competition, game, player, scoreSummary);
    assertTrue(competitionFinishedCard.length > 0);
    File out = new File("E:\\temp\\out.png");
    if(out.exists()) {
      out.delete();
    }
    IOUtils.write(competitionFinishedCard, new FileOutputStream(out));
  }

}
