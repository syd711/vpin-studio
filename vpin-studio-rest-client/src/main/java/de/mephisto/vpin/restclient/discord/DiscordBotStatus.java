package de.mephisto.vpin.restclient.discord;

public class DiscordBotStatus {
  private long botId;
  private String botInitials;
  private boolean valid;
  private boolean validDefaultChannel;
  private long serverId;
  private long categoryId;

  public long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(long categoryId) {
    this.categoryId = categoryId;
  }

  public long getServerId() {
    return serverId;
  }

  public void setServerId(long serverId) {
    this.serverId = serverId;
  }

  public String getBotInitials() {
    return botInitials;
  }

  public void setBotInitials(String botInitials) {
    this.botInitials = botInitials;
  }

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
