package de.mephisto.vpin.connectors.discord;

import java.util.Date;

public class DiscordMessage {
  private DiscordMember member;
  private String raw;
  private Date createdAt;
  private long serverId;

  public long getServerId() {
    return serverId;
  }

  public void setServerId(long serverId) {
    this.serverId = serverId;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public DiscordMember getMember() {
    return member;
  }

  public void setMember(DiscordMember member) {
    this.member = member;
  }

  public String getRaw() {
    return raw;
  }

  public void setRaw(String raw) {
    this.raw = raw;
  }
}
