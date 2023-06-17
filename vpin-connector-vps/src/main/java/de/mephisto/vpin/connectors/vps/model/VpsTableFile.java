package de.mephisto.vpin.connectors.vps.model;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class VpsTableFile extends VpsAuthoredUrls {
  private String id;
  private String comment;
  private List<String> features;
  private String tableFormat;

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
      builder.append(String.join(", ", getAuthors()));
    }

    if(getVersion() != null && getVersion().trim().length() > 0) {
      builder.append(" ");
      builder.append("(");
      builder.append("Version ");
      builder.append(getVersion());
      builder.append(", ");
      builder.append(DateFormat.getDateInstance().format(new Date(getUpdatedAt())));
      builder.append(")");
    }

    return builder.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VpsTableFile)) return false;

    VpsTableFile that = (VpsTableFile) o;

    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
