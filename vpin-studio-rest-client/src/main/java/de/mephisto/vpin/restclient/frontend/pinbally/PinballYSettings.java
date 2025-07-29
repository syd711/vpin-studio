package de.mephisto.vpin.restclient.frontend.pinbally;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

/**
 *
 */
public class PinballYSettings extends JsonSettings {
  private String charset = "windows-1252";

  public String getCharset() {
    return charset;
  }

  public void setCharset(String charset) {
    this.charset = charset;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.PINBALLY_SETTINGS;
  }
}
