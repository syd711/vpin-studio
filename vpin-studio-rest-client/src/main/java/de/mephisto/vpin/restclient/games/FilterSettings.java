package de.mephisto.vpin.restclient.games;

public class FilterSettings {
  private boolean missingAssets;
  private boolean vpsUpdates;
  private boolean versionUpdates;

  public boolean isMissingAssets() {
    return missingAssets;
  }

  public void setMissingAssets(boolean missingAssets) {
    this.missingAssets = missingAssets;
  }

  public boolean isVpsUpdates() {
    return vpsUpdates;
  }

  public void setVpsUpdates(boolean vpsUpdates) {
    this.vpsUpdates = vpsUpdates;
  }

  public boolean isVersionUpdates() {
    return versionUpdates;
  }

  public void setVersionUpdates(boolean versionUpdates) {
    this.versionUpdates = versionUpdates;
  }
}
