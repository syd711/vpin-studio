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

  private final static String testTopic = "Active Competition Table: 'AC-DC'\n" +
      "\n" +
      "data:ew0KICAidXVpZCIgOiAiMjNiZWM0ZWQtZTlhZS00ZGJmLWJkNmMtYTMwOTIwNDUwZjQxIiwNCiAgIm93bmVyIiA6ICIxMDU1MTAzMzIyODc0NDY2MzY1IiwNCiAgInJvbSIgOiAiYWNkXzE2OGgiLA0KICAiZmlsZVNpemUiIDogODAzODQwMDAsDQogICJjcmVhdGVkQXQiIDogMTY3NDkyODQwNjIzMSwNCiAgInN0YXJ0RGF0ZSIgOiAxNjc0Nzc0MDAwMDAwLA0KICAiZW5kRGF0ZSIgOiAxNjc1Mzc4ODAwMDAwLA0KICAibmFtZSIgOiAiTXkgbmV4dCBjb21wZXRpdGlvbiIsDQogICJ0YWJsZU5hbWUiIDogIkFDLURDIiwNCiAgInN0YXJ0TWVzc2FnZUlkIiA6ICIxMDY4NTcxNDczMjc2NTc5OTIwIiwNCiAgInNjb3JlcyIgOiBbIHsNCiAgICAicG9zaXRpb24iIDogMSwNCiAgICAiaW5pdGlhbHMiIDogIk1BQiIsDQogICAgIm51bWVyaWNTY29yZSIgOiAyLjBFOCwNCiAgICAic2NvcmUiIDogIjIwMC4wMDAuMDAwIg0KICB9LCB7DQogICAgInBvc2l0aW9uIiA6IDIsDQogICAgImluaXRpYWxzIiA6ICJQTUwiLA0KICAgICJudW1lcmljU2NvcmUiIDogMS41RTgsDQogICAgInNjb3JlIiA6ICIxNTAuMDAwLjAwMCINCiAgfSwgew0KICAgICJwb3NpdGlvbiIgOiAzLA0KICAgICJpbml0aWFscyIgOiAiVkxLIiwNCiAgICAibnVtZXJpY1Njb3JlIiA6IDEuMzVFOCwNCiAgICAic2NvcmUiIDogIjEzNS4wMDAuMDAwIg0KICB9IF0NCn0=";

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
    System.out.println(encode.length());

  }
}
