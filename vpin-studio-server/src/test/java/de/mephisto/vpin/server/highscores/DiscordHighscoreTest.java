package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionChangeListener;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.players.Player;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class DiscordHighscoreTest extends AbstractVPinServerTest {

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private GameService gameService;

  @Autowired
  private DiscordService discordService;

  @Autowired
  private HighscoreService highscoreService;

  @Test
  public void testCompetitions() throws InterruptedException {
    Game game = gameService.getGameByFilename(AbstractVPinServerTest.TEST_GAME_FILENAME);

    competitionService.addCompetitionChangeListener(new CompetitionChangeListener() {
      @Override
      public void competitionStarted(@NotNull Competition competition) {

      }

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
    String uuid = competitionData.getUuid();
    Competition competition = competitionService.getCompetitionForUuid(uuid);

    if (competition == null) {
      competition = new Competition();
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
      competition.setUuid(uuid);

      Competition save = competitionService.save(competition);
      assertNotNull(save);
    }

    assertNotNull(competitionData);
    assertEquals(competitionData.getScrs().size(), 3);
    assertEquals(competitionData.getRom(), game.getRom());

    HighscoreMetadata metadata = highscoreService.scanScore(game);
    String raw = metadata.getRaw();
    raw = raw.replace("BRE     7.000.000.000", "BOT     6.900.000.000");
    metadata.setRaw(raw);
    highscoreService.updateHighscore(game, metadata);

    Thread.sleep(2000);

    raw = raw.replace("BOT     6.900.000.000", "MFA     7.950.000.000");
    metadata.setRaw(raw);
    highscoreService.updateHighscore(game, metadata);

    Thread.sleep(4000);
  }
}
