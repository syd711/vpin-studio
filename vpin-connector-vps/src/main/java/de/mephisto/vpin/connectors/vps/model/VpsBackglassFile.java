package de.mephisto.vpin.connectors.vps.model;

import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;

public class VpsBackglassFile extends VpsAuthoredUrls {
  private String id;
  private String comment;


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
    if(comment != null) {
      builder.append("- Comment: ");
      builder.append(comment);
      builder.append("\n");
    }

    builder.append("- Created At: ");
    builder.append(DateFormat.getDateTimeInstance().format(new Date(getCreatedAt())));
    return builder.toString();
  }


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VpsBackglassFile)) return false;

    VpsBackglassFile that = (VpsBackglassFile) o;

    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
