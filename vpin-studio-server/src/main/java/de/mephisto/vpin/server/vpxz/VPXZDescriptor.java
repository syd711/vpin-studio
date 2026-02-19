package de.mephisto.vpin.server.vpxz;


import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.vpxz.VPXZPackageInfo;

import java.util.Date;
import java.util.Objects;

public class VPXZDescriptor {
  private TableDetails tableDetails;
  private VPXZPackageInfo packageInfo;
  private VPXZSource source;
  private Date createdAt;
  private String filename;
  private String absoluteFileName;
  private long size;
  private boolean installed;

  public VPXZDescriptor() {
    //used for JSON serialization
  }

  public VPXZDescriptor(VPXZSource source, TableDetails tableDetails, VPXZPackageInfo packageInfo, Date createdAt, String filename, String absoluteFileName, long size) {
    this.source = source;
    this.tableDetails = tableDetails;
    this.packageInfo = packageInfo;
    this.createdAt = createdAt;
    this.filename = filename;
    this.absoluteFileName= absoluteFileName;
    this.size = size;
  }

  public boolean isInstalled() {
    return installed;
  }

  public void setInstalled(boolean installed) {
    this.installed = installed;
  }

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

  public VPXZSource getSource() {
    return source;
  }

  public void setSource(VPXZSource source) {
    this.source = source;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    VPXZDescriptor that = (VPXZDescriptor) o;
    return size == that.size && Objects.equals(tableDetails, that.tableDetails) && Objects.equals(packageInfo, that.packageInfo) && Objects.equals(source, that.source) && Objects.equals(createdAt, that.createdAt) && Objects.equals(filename, that.filename) && Objects.equals(absoluteFileName, that.absoluteFileName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tableDetails, packageInfo, source, createdAt, filename, absoluteFileName, size);
  }
}
