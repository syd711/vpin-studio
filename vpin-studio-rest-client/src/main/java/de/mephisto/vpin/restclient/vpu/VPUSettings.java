package de.mephisto.vpin.restclient.vpu;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

/**
 *
 */
public class VPUSettings extends JsonSettings {
  private String login;
  private String password;

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.VPU_SETTINGS;
  }
}
