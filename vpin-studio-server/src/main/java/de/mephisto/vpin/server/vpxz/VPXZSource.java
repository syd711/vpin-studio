package de.mephisto.vpin.server.vpxz;

import com.fasterxml.jackson.annotation.JsonInclude;

import de.mephisto.vpin.server.util.IncrementGenerated;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "VPXZSources")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VPXZSource {

  @Column(nullable = false)
  private Instant createdAt;

  @Id
  @IncrementGenerated
  private Long id;

  private String type;

  private String name;

  private String location;

  private String login;

  private String password;

  private String authenticationType;

  private boolean enabled;

  private String settings;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getSettings() {
    return settings;
  }

  public void setSettings(String settings) {
    this.settings = settings;
  }

  public String getAuthenticationType() {
    return authenticationType;
  }

  public void setAuthenticationType(String authenticationType) {
    this.authenticationType = authenticationType;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    VPXZSource that = (VPXZSource) o;
    return Objects.equals(id, that.id) && Objects.equals(location, that.location);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, location);
  }

  @Override
  public String toString() {
    return this.getName();
  }
}
