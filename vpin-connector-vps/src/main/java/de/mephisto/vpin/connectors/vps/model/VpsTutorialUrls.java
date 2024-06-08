package de.mephisto.vpin.connectors.vps.model;

import java.text.DateFormat;
import java.util.Date;

public class VpsTutorialUrls extends VpsAuthoredUrls {
  private String title;
  private String youtubeId;


  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (!getAuthors().isEmpty()) {
      builder.append("- Authors: ");
      builder.append(String.join(", ", getAuthors()));
      builder.append("\n");
    }

    if(getVersion() != null) {
      builder.append("- Version: ");
      builder.append(getVersion());
      builder.append("\n");
    }
    if(title != null) {
      builder.append("- Title: ");
      builder.append(title);
      builder.append("\n");
    }

    builder.append("- Updated At: ");
    builder.append(DateFormat.getDateTimeInstance().format(new Date(getUpdatedAt())));
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
