package de.mephisto.vpin.restclient;

import java.util.ArrayList;
import java.util.List;

public class NVRamList {
  private List<String> entries = new ArrayList<>();

  public List<String> getEntries() {
    return entries;
  }

  public void setEntries(List<String> entries) {
    this.entries = entries;
  }

  public boolean contains(String rom) {
    return entries.stream().anyMatch(e -> e.equalsIgnoreCase(rom));
  }
}
