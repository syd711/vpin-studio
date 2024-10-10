package de.mephisto.vpin.restclient.games;

import java.util.ArrayList;
import java.util.List;

public class GameList {

  private List<GameListItem> items = new ArrayList<>();

  public void addItems(List<GameListItem> items) {
    this.items.addAll(items);
  }

  public List<GameListItem> getItems() {
    return items;
  }

  public void setItems(List<GameListItem> items) {
    this.items = items;
  }
}
