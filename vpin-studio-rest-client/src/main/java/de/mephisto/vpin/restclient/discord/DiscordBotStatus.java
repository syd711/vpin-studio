package de.mephisto.vpin.restclient.discord;

public class DiscordBotStatus {
  private long botId;
  private boolean valid;
  private boolean validDefaultChannel;

  public long getBotId() {
    return botId;
  }

  public void setBotId(long botId) {
    this.botId = botId;
  }

  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }

  public boolean isValidDefaultChannel() {
    return validDefaultChannel;
  }

  public void setValidDefaultChannel(boolean validDefaultChannel) {
    this.validDefaultChannel = validDefaultChannel;
  }
}
