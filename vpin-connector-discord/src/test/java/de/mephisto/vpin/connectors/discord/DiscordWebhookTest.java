package de.mephisto.vpin.connectors.discord;

import org.junit.jupiter.api.Test;

public class DiscordWebhookTest {

  @Test
  public void testWebook() {
    String hook = System.getenv("HOOK");
    if(hook != null) {
      String msg= "Hello <@355824324344676352>!" +"```%s\\n" +
          "```";
      msg = String.format(msg, "2032049234242");
      DiscordWebhook.call(hook, msg);
    }
  }
}
