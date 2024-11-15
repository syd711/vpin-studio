package de.mephisto.vpin.restclient.monitor;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.frontend.VPinScreen;

import java.util.ArrayList;
import java.util.List;

public class MonitoringSettings extends JsonSettings {

  private boolean open = false;
  private MonitoringMode monitoringMode;
  private int refreshInterval = 2;
  private double scaling = 1;
  private List<VPinScreen> disabledScreens = new ArrayList<>();

  public double getScaling() {
    return scaling;
  }

  public void setScaling(double scaling) {
    this.scaling = scaling;
  }

  public boolean isOpen() {
    return open;
  }

  public void setOpen(boolean open) {
    this.open = open;
  }

  public MonitoringMode getMonitoringMode() {
    return monitoringMode;
  }

  public void setMonitoringMode(MonitoringMode monitoringMode) {
    this.monitoringMode = monitoringMode;
  }

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
