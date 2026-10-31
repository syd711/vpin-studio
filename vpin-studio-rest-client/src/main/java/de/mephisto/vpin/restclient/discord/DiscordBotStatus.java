package de.mephisto.vpin.restclient.discord;

public class DiscordBotStatus {
  private long botId;
  private String name;
  private String error;

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getBotId() {
    return botId;
  }

  public void setBotId(long botId) {
    this.botId = botId;
  }

}
