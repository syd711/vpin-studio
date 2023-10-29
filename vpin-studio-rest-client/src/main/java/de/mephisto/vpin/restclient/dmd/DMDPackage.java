package de.mephisto.vpin.restclient.dmd;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DMDPackage {
  private String name;
  private DMDPackageTypes dmdPackageTypes;
  private Date modificationDate;
  private List<String> files = new ArrayList<>();
  private long size;

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

  public DMDPackageTypes getDmdPackageTypes() {
    return dmdPackageTypes;
  }

  public void setDmdPackageTypes(DMDPackageTypes dmdPackageTypes) {
    this.dmdPackageTypes = dmdPackageTypes;
  }

  public Date getModificationDate() {
    return modificationDate;
  }

  public void setModificationDate(Date modificationDate) {
    this.modificationDate = modificationDate;
  }

  public List<String> getFiles() {
    return files;
  }

  public void setFiles(List<String> files) {
    this.files = files;
  }

}
