package de.mephisto.vpin.connectors.discord;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class DiscordClientTest {


  @Test
  public void testDiscordClient() throws Exception {
    File f = new File("C:\\vPinball\\VisualPinball\\VPinMAME\\roms\\TAF_L7.zip");
    System.out.println(f.exists());
    System.out.println(f.getCanonicalPath());
//    DiscordClient discordClient = new DiscordClient(token, channelId);
//    discordClient.sendMessage("Allgemein", new File("../resources/highscore-card-sample.png"));
  }
}
