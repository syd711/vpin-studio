package de.mephisto.vpin.connectors.discord;

import java.time.OffsetDateTime;

public class DiscordTextChannel {
  private long id;
  private String name;
  private OffsetDateTime creationDate;

  public OffsetDateTime getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(OffsetDateTime creationDate) {
    this.creationDate = creationDate;
  }

  public long id() {
    return id;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
