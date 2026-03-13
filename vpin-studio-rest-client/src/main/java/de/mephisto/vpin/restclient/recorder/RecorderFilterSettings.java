package de.mephisto.vpin.restclient.recorder;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.FilterSettings;

public class RecorderFilterSettings extends FilterSettings {

  @Override
  public String getSettingsName() {
    return PreferenceNames.RECORDINGS_FILTER_SETTINGS;
  }
}
