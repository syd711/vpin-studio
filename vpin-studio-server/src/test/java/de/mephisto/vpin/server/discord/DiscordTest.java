package de.mephisto.vpin.server.discord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thoughtworks.xstream.core.util.Base64Encoder;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionScoreEntry;
import de.mephisto.vpin.server.highscores.Score;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DiscordTest {

  private final static String testTopic = "Competition Table: Attack from Mars 2.0.1\n" +
      "\n" +
      "data:ew0KICAidXVpZCIgOiAiOWJhMjNhMjgtNzVkMi00MTI4LTliZGItMDYyOWRhMDdlZTg1IiwNCiAgIm93bmVyIiA6ICIxMDU1MTAzMzIyODc0NDY2MzY1IiwNCiAgInJvbSIgOiAiYWZtXzExM2IiLA0KICAiZmlsZVNpemUiIDogMTQxNjMxNDg4LA0KICAic3RhcnREYXRlIiA6IDE2NzU0MjgyODk5ODAsDQogICJlbmREYXRlIiA6IDE2NzU3NzM4ODk5ODAsDQogICJuYW1lIiA6ICJDb21wZXRpdGlvbiBmb3IgQXR0YWNrIGZyb20gTWFycyAyLjAuMSIsDQogICJ0YWJsZU5hbWUiIDogIkF0dGFjayBmcm9tIE1hcnMgMi4wLjEiLA0KICAic3RhcnRNZXNzYWdlSWQiIDogIjEwNzEwNDg2OTIyNDY3ODIwNzciLA0KICAic2NvcmVzIiA6IFsgew0KICAgICJwb3NpdGlvbiIgOiAxLA0KICAgICJpbml0aWFscyIgOiAiU0xMIiwNCiAgICAic2NvcmUiIDogIjcuNTAwLjAwMC4wMDAiDQogIH0sIHsNCiAgICAicG9zaXRpb24iIDogMiwNCiAgICAiaW5pdGlhbHMiIDogIk1GQSIsDQogICAgInNjb3JlIiA6ICI3LjEwMC4wMDAuMDAwIg0KICB9LCB7DQogICAgInBvc2l0aW9uIiA6IDMsDQogICAgImluaXRpYWxzIiA6ICJMRlMiLA0KICAgICJzY29yZSIgOiAiNi41MDAuMDAwLjAwMCINCiAgfSBdDQp9";

  @Test
  public void testTopicHelper() {
    DiscordCompetitionData competitionData = CompetitionDataHelper.getCompetitionData(testTopic);
    assertNotNull(competitionData);
    System.out.println(competitionData.getTableName());
    System.out.println(competitionData.getName());

    List<DiscordCompetitionScoreEntry> scores = competitionData.getScores();
    System.out.println("Scores: ");
    for (DiscordCompetitionScoreEntry score : scores) {
      System.out.println("\t" + score);
    }

    assertFalse(testTopic.length() > 1024);
  }

  @Test
  public void testDataLength() throws JsonProcessingException {
    DiscordCompetitionData data= new DiscordCompetitionData();
    data.setEndDate(new Date());
    data.setStartDate(new Date());
    data.setRom("1234567890123456");
    data.setUuid(UUID.randomUUID().toString());
    data.setOwner("355824324344676352");
    data.setName("1234567890123456789012345678901234567890");
    data.setFileSize(1234567890);
    data.setStartMessageId("1055103322874466365");
    data.setTableName("123456789 123456789 123456789 123456789 ");

    Score score = new Score(new Date(), 1234, "123", null, "100.100.000.000", 100000000000d, 1);
    data.getScores().add(CompetitionDataHelper.toScoreEntry(score));
    score.setPosition(2);
    data.getScores().add(CompetitionDataHelper.toScoreEntry(score));
    score.setPosition(3);
    data.getScores().add(CompetitionDataHelper.toScoreEntry(score));

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    String json = "Competition: " + data.getTableName() + "\n\ndata:" + objectMapper.writeValueAsString(data);
    System.out.println(json.length());
    System.out.println(json);

    String encode = new Base64Encoder().encode(json.getBytes(StandardCharsets.UTF_8));
    assertTrue(encode.length() < 1024);
  }
}
