package de.mephisto.vpin.restclient;

import java.util.ArrayList;
import java.util.List;

public class ResourceList {
  private List<String> items = new ArrayList<>();

  public List<String> getItems() {
    return items;
  }

  public void setItems(List<String> items) {
    this.items = items;
  }
}
