package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import org.junit.jupiter.api.Test;

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
}
