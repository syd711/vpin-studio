package de.mephisto.vpin.server.popper;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
  private int id;
  private Integer menuColor;
  private String name;
  private String playListSQL;
  private boolean sqlPlayList;
  private List<Integer> gameIds = new ArrayList<>();

  public boolean isSqlPlayList() {
    return sqlPlayList;
  }

  public void setSqlPlayList(boolean sqlPlayList) {
    this.sqlPlayList = sqlPlayList;
  }

  public String getPlayListSQL() {
    return playListSQL;
  }

  public void setPlayListSQL(String playListSQL) {
    this.playListSQL = playListSQL;
  }

  public List<Integer> getGameIds() {
    return gameIds;
  }

  public void setGameIds(List<Integer> gameIds) {
    this.gameIds = gameIds;
  }

  public Integer getMenuColor() {
    return menuColor;
  }

  public void setMenuColor(Integer menuColor) {
    this.menuColor = menuColor;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Playlist)) return false;

    Playlist playlist = (Playlist) o;

    return id == playlist.id;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public String toString() {
    return name;
  }
}
