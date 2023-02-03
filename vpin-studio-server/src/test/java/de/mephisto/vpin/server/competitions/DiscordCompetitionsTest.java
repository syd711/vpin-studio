package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreTestUtil;
import de.mephisto.vpin.server.notifications.NotificationService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class DiscordCompetitionsTest extends AbstractVPinServerTest {

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private GameService gameService;

  @Autowired
  private DiscordService discordService;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private PlayerService playerService;

  private String RAW = "GRAND CHAMPION\n" +
      "SLL      52.000.000\n" +
      "\n" +
      "HIGHEST SCORES\n" +
      "1) BRE      44.000.000\n" +
      "2) LFS      40.000.000\n" +
      "3) ZAP      36.000.000\n" +
      "4) RCF      32.000.000\n" +
      "\n" +
      "CASTLE CHAMPION\n";

  @Test
  public void testCompetitions() throws InterruptedException {
    Game game = gameService.getGameByFilename(AbstractVPinServerTest.TEST_GAME_FILENAME);

    competitionService.addCompetitionChangeListener(new CompetitionChangeListener() {
      @Override
      public void competitionCreated(@NonNull Competition competition) {
        assertNotNull(competition);
      }

      @Override
      public void competitionChanged(@NonNull Competition competition) {
        assertNotNull(competition);
      }

      @Override
      public void competitionFinished(@NonNull Competition competition, @Nullable Player player) {
        assertNotNull(competition);
      }

      @Override
      public void competitionDeleted(@NonNull Competition competition) {
        assertNotNull(competition);
      }
    });


    DiscordCompetitionData competitionData = discordService.getCompetitionData(1043199618172858500l, 1043199618172858503l);
    if (competitionData == null) {
      Competition competition = new Competition();
      competition.setGameId(game.getId());
      competition.setName("Competition for " + game.getGameDisplayName());
      competition.setStartDate(new Date());
      competition.setDiscordChannelId(1043199618172858503l);
      competition.setDiscordServerId(1043199618172858500l);
      competition.setOwner("1055103322874466365");
      competition.setType(CompetitionType.DISCORD.name());

      Calendar c = Calendar.getInstance();
      c.setTime(new Date());
      c.add(Calendar.DATE, 4);
      Date newEndDate = c.getTime();
      competition.setEndDate(newEndDate);
      competition.setCreatedAt(new Date());
      competition.setUuid(UUID.randomUUID().toString());

      Competition save = competitionService.save(competition);
      assertNotNull(save);
      competitionData = discordService.getCompetitionData(1043199618172858500l, 1043199618172858503l);
    }

    Competition storedCompetition = competitionService.getCompetitionForUuid(competitionData.getUuid());
    notificationService.highscoreChanged(HighscoreTestUtil.createDiscordHighscoreChangeEvent(storedCompetition, RAW, game));

    ScoreSummary competitionScore = competitionService.getCompetitionScore(storedCompetition.getId());


//    boolean delete = competitionService.delete(save.getId());
//    assertTrue(delete);
    Thread.sleep(4000);
  }
}
