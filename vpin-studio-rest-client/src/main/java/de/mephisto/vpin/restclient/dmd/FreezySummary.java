package de.mephisto.vpin.restclient.dmd;

import java.util.ArrayList;
import java.util.List;

public class FreezySummary {
  private String vniKey;
  private List<String> plugins = new ArrayList<>();
  private String status;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getVniKey() {
    return vniKey;
  }

  public void setVniKey(String vniKey) {
    this.vniKey = vniKey;
  }

  public List<String> getPlugins() {
    return plugins;
  }

  public void setPlugins(List<String> plugins) {
    this.plugins = plugins;
  }
}
