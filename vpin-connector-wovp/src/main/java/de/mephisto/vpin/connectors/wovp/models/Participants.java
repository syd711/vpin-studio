package de.mephisto.vpin.connectors.wovp.models;

import java.util.ArrayList;
import java.util.List;

public class Participants {
  private List<Participant> items = new ArrayList<>();

  public List<Participant> getItems() {
    return items;
  }

  public void setItems(List<Participant> items) {
    this.items = items;
  }
}
