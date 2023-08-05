package de.mephisto.vpin.restclient.discord;

import java.util.List;

public class DiscordServer {
  private String name;
  private long id;
  private String avatarUrl;
  private long ownerId;
  private List<DiscordCategory> categories;

  public List<DiscordCategory> getCategories() {
    return categories;
  }

  public void setCategories(List<DiscordCategory> categories) {
    this.categories = categories;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DiscordServer that = (DiscordServer) o;

    return id == that.id;
  }

  @Override
  public int hashCode() {
    return (int) (id ^ (id >>> 32));
  }

  @Override
  public String toString() {
    return this.getName();
  }
}
