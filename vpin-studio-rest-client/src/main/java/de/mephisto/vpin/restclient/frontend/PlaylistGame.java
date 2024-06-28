package de.mephisto.vpin.restclient.frontend;

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
}
