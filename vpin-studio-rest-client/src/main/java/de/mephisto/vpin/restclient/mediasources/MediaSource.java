package de.mephisto.vpin.restclient.mediasources;

import java.util.Objects;
import java.util.UUID;

public class MediaSource {
  private String id = UUID.randomUUID().toString();
  private String name;
  private MediaSourceType type;
  private String location;
  private boolean enabled;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public MediaSourceType getType() {
    return type;
  }

  public void setType(MediaSourceType type) {
    this.type = type;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    MediaSource that = (MediaSource) o;
    return id == that.id && enabled == that.enabled && Objects.equals(name, that.name) && type == that.type && Objects.equals(location, that.location);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, type, location, enabled);
  }
}
