package de.mephisto.vpin.restclient.monitor;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.frontend.VPinScreen;

import java.util.ArrayList;
import java.util.List;

public class MonitoringSettings extends JsonSettings {

  private int refreshInterval = 2;
  private List<VPinScreen> disabledScreens = new ArrayList<>();

  public int getRefreshInterval() {
    return refreshInterval;
  }

  public void setRefreshInterval(int refreshInterval) {
    this.refreshInterval = refreshInterval;
  }

  public List<VPinScreen> getDisabledScreens() {
    return disabledScreens;
  }

  public void setDisabledScreens(List<VPinScreen> disabledScreens) {
    this.disabledScreens = disabledScreens;
  }
}
