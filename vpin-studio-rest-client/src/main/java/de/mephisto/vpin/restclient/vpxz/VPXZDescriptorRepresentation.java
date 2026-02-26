package de.mephisto.vpin.restclient.vpxz;

import de.mephisto.vpin.restclient.frontend.TableDetails;

import java.util.Date;
import java.util.Objects;

public class VPXZDescriptorRepresentation {
  private TableDetails tableDetails;
  private VPXZSourceRepresentation source;
  private Date createdAt;
  private String filename;
  private long size;
  private String archiveType;
  private String absoluteFileName;
  private VPXZPackageInfo packageInfo;

  public String getAbsoluteFileName() {
    return absoluteFileName;
  }

  public void setAbsoluteFileName(String absoluteFileName) {
    this.absoluteFileName = absoluteFileName;
  }

  public VPXZPackageInfo getPackageInfo() {
    return packageInfo;
  }

  public void setPackageInfo(VPXZPackageInfo packageInfo) {
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

  public VPXZSourceRepresentation getSource() {
    return source;
  }

  public void setSource(VPXZSourceRepresentation source) {
    this.source = source;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VPXZDescriptorRepresentation)) return false;

    VPXZDescriptorRepresentation that = (VPXZDescriptorRepresentation) o;

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
