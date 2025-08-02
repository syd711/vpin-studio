package de.mephisto.vpin.server.backups;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "BackupSources")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BackupSource {

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date createdAt;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
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

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
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
    if (!(o instanceof BackupSource)) return false;

    BackupSource backupSource = (BackupSource) o;

    if (!id.equals(backupSource.id)) return false;
    return location.equals(backupSource.location);
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + location.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return this.getName();
  }
}
