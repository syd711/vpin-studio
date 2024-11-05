package de.mephisto.vpin.restclient.monitor;

public enum MonitoringMode {
  frontendScreens, monitors;


  @Override
  public String toString() {
    switch (this) {
      case monitors: {
        return "All Monitors";
      }
      case frontendScreens: {
        return "Frontend Screens";
      }
    }
    throw new UnsupportedOperationException("Unknown MonitoringMode");
  }
}
