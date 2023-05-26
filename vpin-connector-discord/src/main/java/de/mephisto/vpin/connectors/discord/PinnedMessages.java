package de.mephisto.vpin.connectors.discord;

import java.util.ArrayList;
import java.util.List;

public class PinnedMessages {
  private List<DiscordMessage> messages = new ArrayList<>();

  public List<DiscordMessage> getMessages() {
    return messages;
  }

  public void setMessages(List<DiscordMessage> messages) {
    this.messages = messages;
  }
}
