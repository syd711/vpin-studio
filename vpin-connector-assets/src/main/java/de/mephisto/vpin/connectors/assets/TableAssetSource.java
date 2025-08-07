package de.mephisto.vpin.connectors.assets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TableAssetSource {
  private String id = UUID.randomUUID().toString();
  private String name;
  private String location;
  private boolean enabled;
  private TableAssetSourceType type;

  private String assetSearchLabel;
  private String assetSearchIcon;

  private List<String> supportedScreens = new ArrayList<>();

  public boolean supportsScreen(String screen) {
    return supportsScreens(Arrays.asList(screen));
  }

  public boolean supportsScreens(List<String> screenNames) {
    if (supportedScreens == null || supportedScreens.isEmpty()) {
      return true;
    }

    for (String supportedScreen : supportedScreens) {
      for (String screenName : screenNames) {
        if (screenName.toLowerCase().contains(supportedScreen)) {
          return true;
        }
      }
    }

    return false;
  }

  public List<String> getSupportedScreens() {
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
}
