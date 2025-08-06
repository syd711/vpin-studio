package de.mephisto.vpin.connectors.assets;

import java.util.Objects;
import java.util.UUID;

public class TableAssetSource {
  private String id = UUID.randomUUID().toString();
  private String name;
  private String location;
  private boolean enabled;
  private TableAssetSourceType type;

  private String assetSearchLabel;
  private String assetSearchIcon;

  public TableAssetSourceType getType() {
    return type;
  }

  public void setType(TableAssetSourceType type) {
    this.type = type;
  }

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

  public String getAssetSearchLabel() {
    return assetSearchLabel;
  }

  public void setAssetSearchLabel(String assetSearchLabel) {
    this.assetSearchLabel = assetSearchLabel;
  }

  public String getAssetSearchIcon() {
    return assetSearchIcon;
  }

  public void setAssetSearchIcon(String assetSearchIcon) {
    this.assetSearchIcon = assetSearchIcon;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    TableAssetSource that = (TableAssetSource) o;
    return enabled == that.enabled && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(location, that.location) && type == that.type && Objects.equals(assetSearchLabel, that.assetSearchLabel) && Objects.equals(assetSearchIcon, that.assetSearchIcon);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, location, enabled, type, assetSearchLabel, assetSearchIcon);
  }

  @Override
  public String toString() {
    return "TableAssetSource{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", type=" + type +
        '}';
  }
}
