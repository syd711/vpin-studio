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

    if (!Objects.equals(title, that.title)) return false;
    return Objects.equals(youtubeId, that.youtubeId);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (title != null ? title.hashCode() : 0);
    result = 31 * result + (youtubeId != null ? youtubeId.hashCode() : 0);
    return result;
  }
}
