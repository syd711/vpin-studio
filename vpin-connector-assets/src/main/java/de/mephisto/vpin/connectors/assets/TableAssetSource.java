package de.mephisto.vpin.connectors.assets;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

public class TableAssetSource {
  private String id = UUID.randomUUID().toString();
  private String name;
  private String location;
  private boolean enabled;
  private TableAssetSourceType type;
  private AssetLookupStrategy lookupStrategy = AssetLookupStrategy.autoDetect;

  private String assetSearchLabel;
  private String assetSearchIcon;

  private List<String> supportedScreens = new ArrayList<>();

  @JsonIgnore
  public boolean isProvided() {
    return TableAssetSourceType.PinballX.equals(this.type) || TableAssetSourceType.PinUPPopper.equals(this.type);
  }

  public AssetLookupStrategy getLookupStrategy() {
    if (lookupStrategy == null) {
      lookupStrategy = AssetLookupStrategy.autoDetect;
    }
    return lookupStrategy;
  }

  public void setLookupStrategy(AssetLookupStrategy lookupStrategy) {
    this.lookupStrategy = lookupStrategy;
  }


  public boolean supportsScreen(String screen) {
    return supportsScreens(Arrays.asList(screen));
  }

  public boolean supportsScreens(List<String> screenNames) {
    if (supportedScreens == null || supportedScreens.isEmpty()) {
      return true;
    }

    for (String supportedScreen : supportedScreens) {
      for (String screenName : screenNames) {
        if (screenName.toLowerCase().contains(supportedScreen) || supportedScreen.contains(screenName)) {
          return true;
        }
      }
    }

    return false;
  }

  public List<String> getSupportedScreens() {
    if (supportedScreens == null) {
      supportedScreens = new ArrayList<>();
    }
    return supportedScreens;
  }

  public void setSupportedScreens(List<String> supportedScreens) {
    this.supportedScreens = supportedScreens;
  }

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
