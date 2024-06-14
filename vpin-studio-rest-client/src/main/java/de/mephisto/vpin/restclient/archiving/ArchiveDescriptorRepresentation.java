package de.mephisto.vpin.restclient.archiving;

import de.mephisto.vpin.restclient.frontend.TableDetails;

import java.util.Date;
import java.util.Objects;

public class ArchiveDescriptorRepresentation {
  private TableDetails tableDetails;
  private ArchiveSourceRepresentation source;
  private Date createdAt;
  private String filename;
  private long size;
  private String archiveType;
  private ArchivePackageInfo packageInfo;

  public ArchivePackageInfo getPackageInfo() {
    return packageInfo;
  }

  public void setPackageInfo(ArchivePackageInfo packageInfo) {
    this.packageInfo = packageInfo;
  }

  public String getArchiveType() {
    return archiveType;
  }

  public void setArchiveType(String archiveType) {
    this.archiveType = archiveType;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public TableDetails getTableDetails() {
    return tableDetails;
  }

  public void setTableDetails(TableDetails tableDetails) {
    this.tableDetails = tableDetails;
  }

  public ArchiveSourceRepresentation getSource() {
    return source;
  }

  public void setSource(ArchiveSourceRepresentation source) {
    this.source = source;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ArchiveDescriptorRepresentation)) return false;

    ArchiveDescriptorRepresentation that = (ArchiveDescriptorRepresentation) o;

    return Objects.equals(filename, that.filename);
  }

  @Override
  public int hashCode() {
    return filename != null ? filename.hashCode() : 0;
  }

  @Override
  public String toString() {
    return filename;
  }
}
