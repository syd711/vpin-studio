package de.mephisto.vpin.commons.fx.pausemenu.model;

import de.mephisto.vpin.restclient.games.GameRepresentation;

public class PauseMenuState {
  private GameRepresentation game;
  private boolean apronMode;
  private boolean scoreSubmitterEnabled;

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
