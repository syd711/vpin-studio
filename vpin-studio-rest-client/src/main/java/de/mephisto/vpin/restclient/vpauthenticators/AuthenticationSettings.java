package de.mephisto.vpin.restclient.vpauthenticators;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

/**
 *
 */
public class AuthenticationSettings extends JsonSettings {
  private AuthenticationProvider authenticationProvider;
  private boolean authenticated;
  private String token;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public AuthenticationProvider getAuthenticationProvider() {
    return authenticationProvider;
  }

  public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
    this.authenticationProvider = authenticationProvider;
  }

  public boolean isAuthenticated() {
    return authenticated;
  }

  public void setAuthenticated(boolean authenticated) {
    this.authenticated = authenticated;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.AUTHENTICATION_SETTINGS;
  }
}
