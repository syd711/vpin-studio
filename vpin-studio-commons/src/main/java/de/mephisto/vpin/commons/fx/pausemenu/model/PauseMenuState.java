package de.mephisto.vpin.commons.fx.pausemenu.model;

import de.mephisto.vpin.restclient.games.GameRepresentation;

public class PauseMenuState {
  private GameRepresentation game;
  private boolean apronMode;
  private boolean desktopMode;
  private boolean scoreSubmitterEnabled;
  private int visibleItemCount;

  public int getVisibleItemCount() {
    return visibleItemCount;
  }

  public void setVisibleItemCount(int visibleItemCount) {
    this.visibleItemCount = visibleItemCount;
  }

  public boolean isDesktopMode() {
    return desktopMode;
  }

  public void setDesktopMode(boolean desktopMode) {
    this.desktopMode = desktopMode;
  }

  public boolean isApronMode() {
    return apronMode;
  }

  public void setApronMode(boolean apronMode) {
    this.apronMode = apronMode;
  }

  public GameRepresentation getGame() {
    return game;
  }

  public void setGame(GameRepresentation game) {
    this.game = game;
  }

  public boolean isScoreSubmitterEnabled() {
    return scoreSubmitterEnabled;
  }

  public void setScoreSubmitterEnabled(boolean scoreSubmitterEnabled) {
    this.scoreSubmitterEnabled = scoreSubmitterEnabled;
  }
}
