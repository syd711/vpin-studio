package de.mephisto.vpin.restclient.vpf;

import de.mephisto.vpin.restclient.JsonSettings;

/**
 *
 */
public class VPFSettings extends JsonSettings {
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
}
