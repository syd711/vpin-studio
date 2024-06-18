package de.mephisto.vpin.restclient.frontend.pinballx;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.highscores.HighscoreCardResolution;

/**
 *
 */
public class PinballXSettings extends JsonSettings {
  private String gameExMail;
  private String gameExPassword;
  private boolean gameExEnabled;

  public String getGameExMail() {
    return gameExMail;
  }

  public void setGameExMail(String gameExMail) {
    this.gameExMail = gameExMail;
  }

  public String getGameExPassword() {
    return gameExPassword;
  }

  public void setGameExPassword(String gameExPassword) {
    this.gameExPassword = gameExPassword;
  }

  public boolean isGameExEnabled() {
    return gameExEnabled;
  }

  public void setGameExEnabled(boolean gameExEnabled) {
    this.gameExEnabled = gameExEnabled;
  }
}
