package de.mephisto.vpin.connectors.vps.model;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class VpsTableVersion extends VpsAuthoredUrls {
  private String id;
  private String comment;
  private String imgUrl;
  private List<String> features;
  private String tableFormat;

  public String getImgUrl() {
    return imgUrl;
  }

  public void setImgUrl(String imgUrl) {
    this.imgUrl = imgUrl;
  }

  public String getTableFormat() {
    return tableFormat;
  }

  public void setTableFormat(String tableFormat) {
    this.tableFormat = tableFormat;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<String> getFeatures() {
    return features;
  }

  public void setFeatures(List<String> features) {
    this.features = features;
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
      String authors = String.join(", ", getAuthors());
      if (authors.length() > 40) {
        authors = authors.substring(0, 39) + "...";
      }
      builder.append(authors);
    }

    if (getVersion() != null && getVersion().trim().length() > 0) {
      builder.append(" ");
      builder.append("(");
      builder.append("Version ");
      builder.append(getVersion());
      builder.append(", ");
      builder.append(DateFormat.getDateInstance().format(new Date(getCreatedAt())));
      builder.append(")");
    }

    return builder.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VpsTableVersion)) return false;

    VpsTableVersion that = (VpsTableVersion) o;

    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
