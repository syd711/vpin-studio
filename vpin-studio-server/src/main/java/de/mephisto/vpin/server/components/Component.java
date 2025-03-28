package de.mephisto.vpin.server.components;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.mephisto.vpin.restclient.components.ComponentType;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "Components")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Component {

  @Id
  private ComponentType type;

  private Date lastCheck;

  private String installedVersion;

  private String ignoredVersions;

  private String latestReleaseVersion;

  public String getLatestReleaseVersion() {
    return latestReleaseVersion;
  }

  public void setLatestReleaseVersion(String latestReleaseVersion) {
    this.latestReleaseVersion = latestReleaseVersion;
  }

  public ComponentType getType() {
    return type;
  }

  public void setType(ComponentType type) {
    this.type = type;
  }

  public String getInstalledVersion() {
    return installedVersion;
  }

  public void setInstalledVersion(String installedVersion) {
    this.installedVersion = installedVersion;
  }

  public Date getLastCheck() {
    return lastCheck;
  }

  public void setLastCheck(Date lastCheck) {
    this.lastCheck = lastCheck;
  }

  public String getIgnoredVersions() {
    return ignoredVersions;
  }

  public void setIgnoredVersions(String ignoredVersions) {
    this.ignoredVersions = ignoredVersions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Component that = (Component) o;

    return type.equals(that.type);
  }

  @Override
  public int hashCode() {
    return type.hashCode();
  }

  @Override
  public String toString() {
    return type.toString();
  }
}
