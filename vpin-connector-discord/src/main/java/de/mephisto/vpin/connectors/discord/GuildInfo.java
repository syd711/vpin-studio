package de.mephisto.vpin.connectors.discord;

import net.dv8tion.jda.api.entities.Guild;

public class GuildInfo {
  private String name;
  private long id;
  private String avatarUrl;
  private long ownerId;

  public GuildInfo(Guild guild) {
    this.name = guild.getName();
    this.avatarUrl = guild.getIconUrl();
    this.id = guild.getIdLong();
    this.ownerId = guild.getOwnerIdLong();
  }

  public GuildInfo() {

  }

  public long getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(long ownerId) {
    this.ownerId = ownerId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }
}
