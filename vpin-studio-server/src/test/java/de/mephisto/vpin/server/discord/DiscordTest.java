package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.connectors.discord.*;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.highscores.HighscoreParser;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.ScoreList;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.dv8tion.jda.api.entities.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DiscordTest extends AbstractVPinServerTest {

  private final static long TEST_SERVER_ID = 1043199618172858500l;
  private final static long TEST_CHANNEL_ID = 1066450620522971137l;

  @Autowired
  private HighscoreParser highscoreParser;

  @Autowired
  private DiscordService discordService;

  @Test
  public void testMessageHistory() throws InterruptedException {
    String token = System.getenv("BOT_TOKEN");
    DiscordClient client = new DiscordClient(token, new DiscordCommandResolver() {
      @Override
      public BotCommandResponse resolveCommand(BotCommand cmd) {
        return null;
      }
    });

    assertNotNull(client.getGuilds());

    String competitionUUID = "514103a0-c172-4567-8e1e-c3771263153b";

    DiscordCompetitionData competitionData = discordService.getCompetitionData(TEST_SERVER_ID, TEST_CHANNEL_ID);
    assertNotNull(competitionData);
    assertEquals(competitionData.getUuid(), competitionUUID);

//    ScoreList scoreList = discordService.getScoreList(highscoreParser, competitionUUID, TEST_SERVER_ID, TEST_CHANNEL_ID);
//    assertTrue(scoreList.getScores().isEmpty());

    ScoreList scoreList = discordService.getScoreList(highscoreParser, competitionUUID, TEST_SERVER_ID, TEST_CHANNEL_ID);
    List<ScoreSummary> scores = scoreList.getScores();
    for (ScoreSummary score : scores) {
      List<Score> entries = score.getScores();
      System.out.println("---- " + score.getCreatedAt() + "---------" );
      for (Score entry : entries) {
        System.out.println(entry);
      }

    }


    assertFalse(scores.isEmpty());
  }

  @Test
  public void testMessage() throws Exception {
    String token = System.getenv("BOT_TOKEN");
    DiscordClient client = new DiscordClient(token, new DiscordCommandResolver() {
      @Override
      public BotCommandResponse resolveCommand(BotCommand cmd) {
        return null;
      }
    });

    Message message = client.getMessage(TEST_SERVER_ID, TEST_CHANNEL_ID, 1111981108993732670l);
    assertNotNull(message);
    assertNotNull(message.getMember());
  }

  private ScoreSummary toScoreSummary(@NonNull HighscoreParser highscoreParser, @NonNull DiscordMessage message) {
    List<Score> scores = new ArrayList<>();
    ScoreSummary summary = new ScoreSummary(scores, message.getCreatedAt());
    String raw = message.getRaw();
    scores.addAll(highscoreParser.parseScores(message.getCreatedAt(), raw, -1, message.getServerId()));
    return summary;
  }

}
