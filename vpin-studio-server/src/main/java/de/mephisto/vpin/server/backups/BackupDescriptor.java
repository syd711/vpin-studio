package de.mephisto.vpin.server.backups;


import de.mephisto.vpin.restclient.backups.BackupPackageInfo;
import de.mephisto.vpin.restclient.frontend.TableDetails;

import java.util.Date;

public class BackupDescriptor {
  private TableDetails tableDetails;
  private BackupPackageInfo packageInfo;
  private BackupSource source;
  private Date createdAt;
  private String filename;
  private String absoluteFileName;
  private long size;

  public BackupDescriptor() {
    //used for JSON serialization
  }

  public BackupDescriptor(BackupSource source, TableDetails tableDetails, BackupPackageInfo packageInfo, Date createdAt, String filename, String absoluteFileName, long size) {
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

  public BackupPackageInfo getPackageInfo() {
    return packageInfo;
  }

  public void setPackageInfo(BackupPackageInfo packageInfo) {
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

  public BackupSource getSource() {
    return source;
  }

  public void setSource(BackupSource source) {
    this.source = source;
  }
}
