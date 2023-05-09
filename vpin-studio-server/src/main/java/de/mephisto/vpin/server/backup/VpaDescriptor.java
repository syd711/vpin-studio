package de.mephisto.vpin.server.backup;


import de.mephisto.vpin.restclient.TableManifest;

import java.util.Date;

public class VpaDescriptor {
  private TableManifest manifest;
  private VpaSource source;
  private Date createdAt;
  private String filename;
  private long size;

  public VpaDescriptor(VpaSource source, TableManifest manifest, Date createdAt, String filename, long size) {
    this.source = source;
    this.manifest = manifest;
    this.createdAt = createdAt;
    this.filename = filename;
    this.size = size;
  }

  public long getSize() {
    if (size == 0) {
      //TODO
//      return manifest.getVpaFileSize();
    }
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

  public TableManifest getManifest() {
    return manifest;
  }

  public void setManifest(TableManifest manifest) {
    this.manifest = manifest;
  }

  public VpaSource getSource() {
    return source;
  }

  public void setSource(VpaSource source) {
    this.source = source;
  }
}
