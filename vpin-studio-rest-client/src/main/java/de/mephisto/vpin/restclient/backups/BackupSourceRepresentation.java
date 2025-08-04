package de.mephisto.vpin.restclient.backups;

import java.util.Objects;

public class BackupSourceRepresentation {
  private long id;
  private String name;
  private String type;
  private String location;
  private String login;
  private String password;
  private String settings;
  private boolean enabled;
  private String authenticationType;

  public String getSettings() {
    return settings;
  }

  public void setSettings(String settings) {
    this.settings = settings;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getAuthenticationType() {
    return authenticationType;
  }

  public void setAuthenticationType(String authenticationType) {
    this.authenticationType = authenticationType;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BackupSourceRepresentation)) return false;

    BackupSourceRepresentation that = (BackupSourceRepresentation) o;

    if (id != that.id) return false;
    if (enabled != that.enabled) return false;
    if (!name.equals(that.name)) return false;
    if (!type.equals(that.type)) return false;
    if (!location.equals(that.location)) return false;
    if (!Objects.equals(login, that.login)) return false;
    if (!Objects.equals(password, that.password)) return false;
    if (!Objects.equals(settings, that.settings)) return false;
    return Objects.equals(authenticationType, that.authenticationType);
  }

  @Override
  public int hashCode() {
    int result = (int) (id ^ (id >>> 32));
    result = 31 * result + name.hashCode();
    result = 31 * result + type.hashCode();
    result = 31 * result + location.hashCode();
    result = 31 * result + (login != null ? login.hashCode() : 0);
    result = 31 * result + (password != null ? password.hashCode() : 0);
    result = 31 * result + (settings != null ? settings.hashCode() : 0);
    result = 31 * result + (enabled ? 1 : 0);
    result = 31 * result + (authenticationType != null ? authenticationType.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
