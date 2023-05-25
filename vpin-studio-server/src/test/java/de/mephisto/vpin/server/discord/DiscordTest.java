package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.connectors.discord.*;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.highscores.HighscoreParser;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.ScoreList;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DiscordTest extends AbstractVPinServerTest {

  private long TEST_SERVER_ID = 1043199618172858500l;
  private long TEST_CHANNEL_ID = 1066450620522971137l;

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

    DiscordCompetitionData competitionData = discordService.getCompetitionData(TEST_SERVER_ID, TEST_CHANNEL_ID, competitionUUID);
    assertNotNull(competitionData);
    assertEquals(competitionData.getUuid(), competitionUUID);

//    ScoreList scoreList = discordService.getScoreList(highscoreParser, competitionUUID, TEST_SERVER_ID, TEST_CHANNEL_ID);
//    assertTrue(scoreList.getScores().isEmpty());

    List<DiscordMessage> messageHistoryAfter = client.getMessageHistoryAfter(TEST_SERVER_ID, TEST_CHANNEL_ID, competitionData.getMsgId(), competitionUUID);
    assertTrue(messageHistoryAfter.size() >= 1);

    List<DiscordMessage> competitionUpdates = client.getCompetitionUpdates(TEST_SERVER_ID, TEST_CHANNEL_ID, competitionData.getMsgId(), competitionUUID);
    List<ScoreSummary> scores = competitionUpdates.stream().map(message -> toScoreSummary(highscoreParser, message)).collect(Collectors.toList());
    for (ScoreSummary score : scores) {
      List<Score> entries = score.getScores();
      System.out.println("---- " + score.getCreatedAt() + "---------" );
      for (Score entry : entries) {
        System.out.println(entry);
      }

    }


    assertFalse(scores.isEmpty());
  }

  private ScoreSummary toScoreSummary(@NonNull HighscoreParser highscoreParser, @NonNull DiscordMessage message) {
    List<Score> scores = new ArrayList<>();
    ScoreSummary summary = new ScoreSummary(scores, message.getCreatedAt());
    String raw = message.getRaw();
    scores.addAll(highscoreParser.parseScores(message.getCreatedAt(), raw, -1, message.getServerId()));
    return summary;
  }

}
