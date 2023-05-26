package de.mephisto.vpin.connectors.discord;

import java.util.Date;

public class DiscordMessage {
  private long id;
  private DiscordMember member;
  private String raw;
  private String embedDescription;
  private Date createdAt;
  private long serverId;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getEmbedDescription() {
    return embedDescription;
  }

  public void setEmbedDescription(String embedDescription) {
    this.embedDescription = embedDescription;
  }

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DiscordMessage)) return false;

    DiscordMessage that = (DiscordMessage) o;

    return id == that.id;
  }

  @Override
  public int hashCode() {
    return (int) (id ^ (id >>> 32));
  }

  @Override
  public String toString() {
    return "Discord Message '" + raw + "' (" + id + ")";
  }
}
