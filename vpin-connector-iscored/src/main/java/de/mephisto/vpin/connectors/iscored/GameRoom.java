package de.mephisto.vpin.connectors.iscored;

import java.util.ArrayList;
import java.util.List;

public class GameRoom {

  private int roomID;
  private String name;

  private List<Game> games = new ArrayList<>();

  private Settings settings;

  public Settings getSettings() {
    return settings;
  }

  public void setSettings(Settings settings) {
    this.settings = settings;
  }

  public int getRoomID() {
    return roomID;
  }

  public void setRoomID(int roomID) {
    this.roomID = roomID;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Game> getGames() {
    return games;
  }

  public void setGames(List<Game> games) {
    this.games = games;
  }
}
