package de.mephisto.vpin.connectors.discord;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class DiscordClientTest {


//  @Test
//  public void testDiscordClient() throws Exception {
//    DiscordClient discordClient = new DiscordClient(token, channelId);
//    discordClient.sendMessage("Allgemein", new File("../resources/highscore-card-sample.png"));
//  }

  @Test
  public void testWebhook() throws IOException {
    String url = "https://discord.com/api/webhooks/1043202008133423186/zWtLf7jqPi5C8GrNqB-svt9xQSJ6QFzQmPrEGBZ3ugezYy6NqvgK03EvZyJJTOTUlkR0";


    String msg = "A new competition has been started!\\n" +
        "```\\n" +
        "%s\\n" +
        "----------------------------------------------------------------------\\n" +
        "Table:       %s\\n" +
        "Start Date:  %s\\n" +
        "End Date:    %s\\n" +
        "Duration:    %s\\n" +
        "----------------------------------------------------------------------```";

    String finishMsg =
        "```" +
        "The competition '%s' has been finished!\\n" +
        "And the winner is...\\n" +
        "\\n" +
        "        %s\\n" +
        "\\n" +
        "Table: %s\\n" +
        "Score: %s\\n" +
        "\\n" +
        "%s" +
        "%s" +
        "```";

    String second = "#2";

    String format = String.format(finishMsg, "Weekly challange 32", "Steve", "Attack from Mars", "7.345.234.234");
    DiscordWebhook.call(url, format);
  }
}
