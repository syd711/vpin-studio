package de.mephisto.vpin.connectors.discord;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class DiscordClientTest {

  @Test
  public void testDiscordClient() throws Exception {
    String token = System.getenv("TOKEN");

    if(token != null) {
      DiscordClient client = new DiscordClient(token, null);
//      client.refreshMembers();
      client.setStatus("testing");

      Thread.sleep(2000);
      List<DiscordMember> members = client.getMembers();
      assertFalse(members.isEmpty());

      Thread.sleep(20000);
      client.shutdown();
    }
  }
}
