package de.mephisto.vpin.restclient;

import java.util.ArrayList;
import java.util.List;

public class SystemData {
  private String path;
  private List<String> items = new ArrayList<>();

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public List<String> getItems() {
    return items;
  }

  public void setItems(List<String> items) {
    this.items = items;
  }
}
