package de.mephisto.vpin.connectors.vps.model;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VpsTutorialUrls extends VpsAuthoredUrls {
  private String title;
  private String youtubeId;
  private String url;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

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

    if(this.url != null) {
      VpsUrl vpsUrl = new VpsUrl();
      vpsUrl.setBroken(false);
      vpsUrl.setUrl(this.url);
      urls.add(vpsUrl);
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

    if (getCreatedAt() != null) {
      builder.append("- Created At: ");
      OffsetDateTime dateTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(getCreatedAt()), ZoneId.systemDefault());
      builder.append(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(dateTime));
    }
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
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    VpsTutorialUrls that = (VpsTutorialUrls) o;
    return Objects.equals(title, that.title) && Objects.equals(youtubeId, that.youtubeId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), title, youtubeId);
  }
}
