package de.mephisto.vpin.restclient.doftester;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

public class DOFTesterSettings extends JsonSettings {
  private int testDuration = 200;

  public int getTestDuration() {
    return testDuration;
  }

  public void setTestDuration(int testDuration) {
    this.testDuration = testDuration;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.DOF_TESTER_SETTINGS;
  }
}
