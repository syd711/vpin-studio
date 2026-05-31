package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

public class VRSettings extends JsonSettings {
  private boolean enabled = false;
  private boolean vrEnabled;

  public boolean isVrEnabled() {
    return vrEnabled;
  }

  public void setVrEnabled(boolean vrEnabled) {
    this.vrEnabled = vrEnabled;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.VR_SETTINGS;
  }
}
