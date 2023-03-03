package de.mephisto.vpin.restclient.representations;

import de.mephisto.vpin.restclient.VpaManifest;

import java.util.Date;
import java.util.Objects;

public class VpaDescriptorRepresentation {
  private VpaManifest manifest;
  private VpaSourceRepresentation source;
  private Date createdAt;
  private String filename;
  private long size;

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

  public VpaManifest getManifest() {
    return manifest;
  }

  public void setManifest(VpaManifest manifest) {
    this.manifest = manifest;
  }

  public VpaSourceRepresentation getSource() {
    return source;
  }

  public void setSource(VpaSourceRepresentation source) {
    this.source = source;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VpaDescriptorRepresentation)) return false;

    VpaDescriptorRepresentation that = (VpaDescriptorRepresentation) o;

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
