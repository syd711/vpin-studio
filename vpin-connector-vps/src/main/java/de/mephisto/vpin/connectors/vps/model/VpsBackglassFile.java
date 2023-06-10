package de.mephisto.vpin.connectors.vps.model;

import java.util.Objects;

public class VpsBackglassFile extends VpsAuthoredUrls {
  private String id;
  private String comment;
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
  public String toString() {
    StringBuilder builder = new StringBuilder();
    String comment = getComment();
    if (comment != null && comment.trim().length() > 0) {
      builder.append(comment);
    }
    else {
      builder.append(String.join(", ", getAuthors()));
    }

    if(getVersion() != null && getVersion().trim().length() > 0) {
      builder.append(" ");
      builder.append("(");
      builder.append("Version ");
      builder.append(getVersion());
      builder.append(")");
    }

    return builder.toString();
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
