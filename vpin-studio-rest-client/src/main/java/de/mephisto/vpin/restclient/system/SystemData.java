package de.mephisto.vpin.restclient.system;

import java.util.ArrayList;
import java.util.List;

public class SystemData {
  private String path;
  private String data;
  private List<String> items = new ArrayList<>();

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

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
