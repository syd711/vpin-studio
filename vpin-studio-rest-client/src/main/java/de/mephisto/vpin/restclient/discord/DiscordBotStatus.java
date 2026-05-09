package de.mephisto.vpin.restclient.discord;

import java.util.ArrayList;
import java.util.List;

public class DiscordBotStatus {
  private long botId;
  private String name;
  private List<String> botInitials = new ArrayList<>();
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

  public List<String> getBotInitials() {
    return botInitials;
  }

  public void setBotInitials(List<String> botInitials) {
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
