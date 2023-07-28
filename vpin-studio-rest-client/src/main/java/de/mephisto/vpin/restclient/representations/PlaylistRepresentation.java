package de.mephisto.vpin.restclient.representations;

import java.util.ArrayList;
import java.util.List;

public class PlaylistRepresentation {
  private int id;
  private String name;
  private Integer menuColor;
  private boolean sqlPlayList;
  private List<Integer> gameIds = new ArrayList<>();

  public boolean isSqlPlayList() {
    return sqlPlayList;
  }

  public void setSqlPlayList(boolean sqlPlayList) {
    this.sqlPlayList = sqlPlayList;
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
  public String toString() {
    return this.name + " (" + this.gameIds.size() + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PlaylistRepresentation that = (PlaylistRepresentation) o;

    return id == that.id;
  }

  @Override
  public int hashCode() {
    return id;
  }
}
