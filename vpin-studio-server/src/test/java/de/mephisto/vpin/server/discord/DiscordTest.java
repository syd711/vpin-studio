package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.server.competitions.ScoreSummary;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DiscordTest {

  private final static String testTopic = "Active Competition Table: 'AC-DC'\n" +
      "\n" +
      "data:ew0KICAidXVpZCIgOiAiYjk4NjA5M2EtZTk0Yy00MTgwLWE4NDktZDM4YTY4NDY5YTRkIiwNCiAgIm93bmVyIiA6IG51bGwsDQogICJoaWdoc2NvcmUiIDogbnVsbCwNCiAgInJvbSIgOiAiYWNkXzE2OGgiLA0KICAiZmlsZVNpemUiIDogODAzODQwMDAsDQogICJzY29yZXMiIDogWyB7DQogICAgInBvc2l0aW9uIiA6IDEsDQogICAgImluaXRpYWxzIiA6ICI0LjIiLA0KICAgICJudW1lcmljU2NvcmUiIDogNC4yOTQ5NjcyOTVFOSwNCiAgICAic2NvcmUiIDogIjQuMjk0Ljk2Ny4yOTUiDQogIH0sIHsNCiAgICAicG9zaXRpb24iIDogMiwNCiAgICAiaW5pdGlhbHMiIDogIj8/PyIsDQogICAgIm51bWVyaWNTY29yZSIgOiAwLjAsDQogICAgInNjb3JlIiA6ICIwIg0KICB9LCB7DQogICAgInBvc2l0aW9uIiA6IDMsDQogICAgImluaXRpYWxzIiA6ICLvv73vv73vv73vv70iLA0KICAgICJudW1lcmljU2NvcmUiIDogMC4wLA0KICAgICJzY29yZSIgOiAiMCINCiAgfSBdDQp9";

  @Test
  public void testTopicHelper() {
    UUID uuid = TopicHelper.getUuid(testTopic);
    assertNotNull(uuid);


    ScoreSummary scores = TopicHelper.getScores(testTopic);
    assertFalse(scores.getScores().isEmpty());
  }
}
