package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.server.players.Player;

public class DiscordPlayer extends Player {
  private boolean bot;

  public boolean isBot() {
    return bot;
  }

  public void setBot(boolean bot) {
    this.bot = bot;
  }
}
