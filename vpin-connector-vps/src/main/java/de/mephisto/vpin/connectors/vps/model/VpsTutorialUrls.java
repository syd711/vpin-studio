package de.mephisto.vpin.connectors.vps.model;

import java.util.Objects;

public class VpsTutorialUrls extends VpsAuthoredUrls {
  private String title;
  private String youtubeId;


  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getYoutubeId() {
    return youtubeId;
  }

  public void setYoutubeId(String youtubeId) {
    this.youtubeId = youtubeId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VpsTutorialUrls)) return false;
    if (!super.equals(o)) return false;

    VpsTutorialUrls that = (VpsTutorialUrls) o;

    if (!title.equals(that.title)) return false;
    return youtubeId.equals(that.youtubeId);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + youtubeId.hashCode();
    return result;
  }
}
