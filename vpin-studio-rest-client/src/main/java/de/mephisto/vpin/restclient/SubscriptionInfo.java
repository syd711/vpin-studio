package de.mephisto.vpin.restclient;

public class SubscriptionInfo {
  public final static int DEFAULT_SCORE_LIMIT = 10;

  private long serverId;
  private long channelId;
  private long ownerId;
  private String uuid;

  public long getServerId() {
    return serverId;
  }

  public void setServerId(long serverId) {
    this.serverId = serverId;
  }

  public long getChannelId() {
    return channelId;
  }

  public void setChannelId(long channelId) {
    this.channelId = channelId;
  }

  public long getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(long ownerId) {
    this.ownerId = ownerId;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }
}
