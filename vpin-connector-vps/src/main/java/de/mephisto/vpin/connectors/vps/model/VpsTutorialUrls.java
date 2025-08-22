package de.mephisto.vpin.connectors.vps.model;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class VpsTutorialUrls extends VpsAuthoredUrls {
  private String title;
  private String youtubeId;

  public List<VpsUrl> getUrls() {
    List<VpsUrl> urls = super.getUrls();
    if (urls == null) {
      urls = new ArrayList<>();
    }
    if (youtubeId != null) {
      VpsUrl url = new VpsUrl();
      url.setUrl("https://www.youtube.com/watch?v=" + youtubeId);
      url.setBroken(false);
      urls.add(url);
    }
    return urls;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (!getAuthors().isEmpty()) {
      builder.append("- Authors: ");
      builder.append(String.join(", ", getAuthors()));
      builder.append("\n");
    }

    if (getVersion() != null) {
      builder.append("- Version: ");
      builder.append(getVersion());
      builder.append("\n");
    }
    if (title != null) {
      builder.append("- Title: ");
      builder.append(title);
      builder.append("\n");
    }

    builder.append("- Created At: ");
    builder.append(DateFormat.getDateTimeInstance().format(new Date(getCreatedAt())));
    return builder.toString();
  }


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
    //if (!super.equals(o)) return false;

    VpsTutorialUrls that = (VpsTutorialUrls) o;

    return StringUtils.equals(this.title, that.title) && StringUtils.equals(this.youtubeId, that.youtubeId);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + youtubeId.hashCode();
    return result;
  }
}
