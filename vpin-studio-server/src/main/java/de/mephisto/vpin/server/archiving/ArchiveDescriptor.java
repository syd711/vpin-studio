package de.mephisto.vpin.server.archiving;


import de.mephisto.vpin.restclient.archiving.ArchivePackageInfo;
import de.mephisto.vpin.restclient.frontend.TableDetails;

import java.util.Date;

public class ArchiveDescriptor {
  private TableDetails tableDetails;
  private ArchivePackageInfo packageInfo;
  private ArchiveSource source;
  private Date createdAt;
  private String filename;
  private String absoluteFileName;
  private long size;

  public ArchiveDescriptor() {
    //used for JSON serialization
  }

  public ArchiveDescriptor(ArchiveSource source, TableDetails tableDetails, ArchivePackageInfo packageInfo, Date createdAt, String filename, String absoluteFileName, long size) {
    this.source = source;
    this.tableDetails = tableDetails;
    this.packageInfo = packageInfo;
    this.createdAt = createdAt;
    this.filename = filename;
    this.absoluteFileName= absoluteFileName;
    this.size = size;
  }

  public String getAbsoluteFileName() {
    return absoluteFileName;
  }

  public void setAbsoluteFileName(String absoluteFileName) {
    this.absoluteFileName = absoluteFileName;
  }

  public ArchivePackageInfo getPackageInfo() {
    return packageInfo;
  }

  public void setPackageInfo(ArchivePackageInfo packageInfo) {
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

  public ArchiveSource getSource() {
    return source;
  }

  public void setSource(ArchiveSource source) {
    this.source = source;
  }
}
