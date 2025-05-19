package de.mephisto.vpin.connectors.iscored;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameRoom {

  private int roomID;
  private String name;
  private String url;

  private List<IScoredGame> games = new ArrayList<>();

  private Settings settings;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

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

  public List<IScoredGame> getGames() {
    return games;
  }

  @JsonIgnore
  public List<IScoredGame> getTaggedGames() {
    return games.stream().filter(g -> g.isVpsTagged()).collect(Collectors.toList());
  }

  public IScoredGame getGameByVps(String vpsTableId, String vpsVersionId) {
    for (IScoredGame game : this.games) {
      if(game.matches(vpsTableId, vpsVersionId)) {
        return game;
      }
    }
    return null;
  }

  public void setGames(List<IScoredGame> games) {
    this.games = games;
  }
}
