package de.mephisto.vpin.restclient.representations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PupPackRepresentation {
  private long size;
  private Date modificationDate;
  private boolean enabled;
  private List<String> options = new ArrayList<>();

  public List<String> getOptions() {
    return options;
  }

  public void setOptions(List<String> options) {
    this.options = options;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Date getModificationDate() {
    return modificationDate;
  }

  public void setModificationDate(Date modificationDate) {
    this.modificationDate = modificationDate;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }
}
