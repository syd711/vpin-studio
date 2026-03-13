package de.mephisto.vpin.restclient.discord;

public class DiscordBotStatus {
  private long botId;
  private String name;
  private String botInitials;
  private boolean validDefaultChannel;
  private boolean canManageCategories;
  private long serverId;
  private long categoryId;
  private String error;

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public boolean isCanManageCategories() {
    return canManageCategories;
  }

  public void setCanManageCategories(boolean canManageCategories) {
    this.canManageCategories = canManageCategories;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

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

  public boolean isValidDefaultChannel() {
    return validDefaultChannel;
  }

  public void setValidDefaultChannel(boolean validDefaultChannel) {
    this.validDefaultChannel = validDefaultChannel;
  }
}
