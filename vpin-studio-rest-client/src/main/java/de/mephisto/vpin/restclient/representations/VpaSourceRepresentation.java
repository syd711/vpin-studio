package de.mephisto.vpin.restclient.representations;

public class VpaSourceRepresentation {
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
    if (this == o) return true;
    if (!(o instanceof VpaSourceRepresentation)) return false;

    VpaSourceRepresentation that = (VpaSourceRepresentation) o;

    if (!name.equals(that.name)) return false;
    if (!type.equals(that.type)) return false;
    return location.equals(that.location);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + type.hashCode();
    result = 31 * result + location.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
