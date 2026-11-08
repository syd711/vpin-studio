package de.mephisto.vpin.restclient.vpf;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.vpauthenticators.CredentialSettings;

/**
 *
 */
public class VPFSettings extends JsonSettings implements CredentialSettings {
  private String login;
  private String password;
  private boolean passwordSet;

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

  public boolean isPasswordSet() {
    return passwordSet;
  }

  public void setPasswordSet(boolean passwordSet) {
    this.passwordSet = passwordSet;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.VPF_SETTINGS;
  }
}
