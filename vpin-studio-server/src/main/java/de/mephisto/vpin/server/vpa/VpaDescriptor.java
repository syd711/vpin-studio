package de.mephisto.vpin.server.vpa;


import de.mephisto.vpin.restclient.VpaManifest;

import java.util.Date;

public class VpaDescriptor {
  private VpaManifest manifest;
  private VpaSource source;
  private Date createdAt;
  private String name;
  private long size;

  public VpaDescriptor(VpaSource source, VpaManifest manifest, Date createdAt, String name, long size) {
    this.source = source;
    this.manifest = manifest;
    this.createdAt = createdAt;
    this.name = name;
    this.size = size;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public VpaManifest getManifest() {
    return manifest;
  }

  public void setManifest(VpaManifest manifest) {
    this.manifest = manifest;
  }

  public VpaSource getSource() {
    return source;
  }

  public void setSource(VpaSource source) {
    this.source = source;
  }
}
