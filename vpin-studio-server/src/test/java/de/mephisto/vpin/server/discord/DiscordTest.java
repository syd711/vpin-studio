package de.mephisto.vpin.server.discord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thoughtworks.xstream.core.util.Base64Encoder;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.server.highscores.Score;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DiscordTest {

  private final static String testTopic = "Competition Table: Attack from Mars 2.0.1\n" +
      "\n" +
      "data:ew0KICAidXVpZCIgOiAiODkyOTRhNDgtZDBjMy00ZDFmLThlOWYtNGM1ZmQyZDk2Yzg1IiwNCiAgIm93bmVyIiA6ICIxMDU1MTAzMzIyODc0NDY2MzY1IiwNCiAgInJvbSIgOiAiYWZtXzExM2IiLA0KICAiZmlsZVNpemUiIDogMTQxNjMxNDg4LA0KICAic3RhcnREYXRlIiA6IDE2NzUwMDE3ODc3NzIsDQogICJlbmREYXRlIiA6IDE2NzUzNDczODc3NzIsDQogICJuYW1lIiA6ICJDb21wZXRpdGlvbiBmb3IgQXR0YWNrIGZyb20gTWFycyAyLjAuMSIsDQogICJ0YWJsZU5hbWUiIDogIkF0dGFjayBmcm9tIE1hcnMgMi4wLjEiLA0KICAic3RhcnRNZXNzYWdlSWQiIDogIjEwNjkyNTk3NTQ1ODY3Njc0NTAiLA0KICAic2NvcmVzIiA6IFsgew0KICAgICJwb3NpdGlvbiIgOiAxLA0KICAgICJpbml0aWFscyIgOiAiU0xMIiwNCiAgICAic2NvcmUiIDogIjcuNTAwLjAwMC4wMDAiDQogIH0sIHsNCiAgICAicG9zaXRpb24iIDogMiwNCiAgICAiaW5pdGlhbHMiIDogIkJSRSIsDQogICAgInNjb3JlIiA6ICI3LjAwMC4wMDAuMDAwIg0KICB9LCB7DQogICAgInBvc2l0aW9uIiA6IDMsDQogICAgImluaXRpYWxzIiA6ICJMRlMiLA0KICAgICJzY29yZSIgOiAiNi41MDAuMDAwLjAwMCINCiAgfSBdDQp9";

  @Test
  public void testTopicHelper() {
    DiscordCompetitionData competitionData = CompetitionDataHelper.getCompetitionData(testTopic);
    assertNotNull(competitionData);
    System.out.println(competitionData.getTableName());
    System.out.println(competitionData.getName());
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
