package de.mephisto.vpin.restclient.frontend;

import java.util.Objects;

public class PlaylistGame {
  private int id;
  private boolean fav;
  private boolean globalFav;
  private boolean played;

  public boolean isPlayed() {
    return played;
  }

  public void setPlayed(boolean played) {
    this.played = played;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public boolean isFav() {
    return fav;
  }

  public void setFav(boolean fav) {
    this.fav = fav;
  }

  public boolean isGlobalFav() {
    return globalFav;
  }

  public void setGlobalFav(boolean globalFav) {
    this.globalFav = globalFav;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PlaylistGame that = (PlaylistGame) o;
    return id == that.id && fav == that.fav && globalFav == that.globalFav && played == that.played;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, fav, globalFav, played);
  }

  @Override
  public String toString() {
    return "PlaylistGame " + getId() + " [" + globalFav + "/" + fav + "]";
  }
}
