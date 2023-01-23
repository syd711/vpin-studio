package de.mephisto.vpin.server.discord;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DiscordTest {

  private final static String testTopic = "Active Competition Table: 'AC-DC'\n" +
      "\n" +
      "data:ew0KICAidXVpZCIgOiAiYTE4YmJhMzQtODNmNi00ODdmLTljYjItZjUzOGFlYmY5MzQyIiwNCiAgIm93bmVyIiA6IG51bGwsDQogICJyb20iIDogImFjZF8xNjhoIiwNCiAgImZpbGVTaXplIiA6IDgwMzg0MDAwLA0KICAiY3JlYXRlZEF0IiA6IDE2NzQ0MDM4MjE4OTAsDQogICJzY29yZXMiIDogWyB7DQogICAgInBvc2l0aW9uIiA6IDEsDQogICAgImluaXRpYWxzIiA6ICI0LjIiLA0KICAgICJudW1lcmljU2NvcmUiIDogNC4yOTQ5NjcyOTVFOSwNCiAgICAic2NvcmUiIDogIjQuMjk0Ljk2Ny4yOTUiDQogIH0sIHsNCiAgICAicG9zaXRpb24iIDogMiwNCiAgICAiaW5pdGlhbHMiIDogIj8/PyIsDQogICAgIm51bWVyaWNTY29yZSIgOiAwLjAsDQogICAgInNjb3JlIiA6ICIwIg0KICB9LCB7DQogICAgInBvc2l0aW9uIiA6IDMsDQogICAgImluaXRpYWxzIiA6ICLvv73vv73vv73vv70iLA0KICAgICJudW1lcmljU2NvcmUiIDogMC4wLA0KICAgICJzY29yZSIgOiAiMCINCiAgfSBdDQp9";

  @Test
  public void testTopicHelper() {
    UUID uuid = CompetitionDataHelper.getUuid(testTopic);
    assertNotNull(uuid);
    System.out.println(testTopic.length());
    assertFalse(testTopic.length() > 1024);
  }
}
