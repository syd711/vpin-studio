package de.mephisto.vpin.restclient.frontend.pinballx;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

/**
 *
 */
public class PinballXSettings extends JsonSettings {
  private String gameExMail;
  private String gameExPassword;
  private boolean gameExEnabled;

  //unlike PinballY which might require e.g. win1252, PinballX is handling UTF-8 properly, so no UI for this field since utf8 is the studio default
  private String charset;

  public String getCharset() {
    return charset;
  }

  public void setCharset(String charset) {
    this.charset = charset;
  }

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

  @Override
  public String getSettingsName() {
    return PreferenceNames.PINBALLX_SETTINGS;
  }
}
