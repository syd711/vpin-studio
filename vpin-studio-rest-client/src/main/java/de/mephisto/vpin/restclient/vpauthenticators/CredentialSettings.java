package de.mephisto.vpin.restclient.vpauthenticators;

/**
 * Marker for JSON settings that contain an account password.
 * <p>
 * The server stores the password encrypted and never returns it via the REST API.
 * Clients only receive {@link #isPasswordSet()} to know whether a password is stored.
 * When a client saves these settings with a blank password, the stored password is kept.
 */
public interface CredentialSettings {

  String getLogin();

  String getPassword();

  void setPassword(String password);

  boolean isPasswordSet();

  void setPasswordSet(boolean passwordSet);
}
