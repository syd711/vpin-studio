package de.mephisto.vpin.restclient.iscored;

import java.util.Objects;

public class IScoredGameRoom {
  private String uuid;
  private String url;
  private boolean synchronize = true;
  private boolean scoreReset = true;
  private String badge;

  public String getBadge() {
    return badge;
  }

  public void setBadge(String badge) {
    this.badge = badge;
  }

  public boolean isScoreReset() {
    return scoreReset;
  }

  public void setScoreReset(boolean scoreReset) {
    this.scoreReset = scoreReset;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }


  public boolean isSynchronize() {
    return synchronize;
  }

  public void setSynchronize(boolean synchronize) {
    this.synchronize = synchronize;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    IScoredGameRoom that = (IScoredGameRoom) o;
    return Objects.equals(uuid, that.uuid);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(uuid);
  }

  @Override
  public String toString() {
    return getUrl();
  }
}
