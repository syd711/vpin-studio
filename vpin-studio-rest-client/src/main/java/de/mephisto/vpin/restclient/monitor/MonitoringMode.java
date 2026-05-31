package de.mephisto.vpin.restclient.monitor;

public enum MonitoringMode {
  frontendScreens, monitors;


  @Override
  public String toString() {
      return switch (this) {
          case monitors -> "All Monitors";
          case frontendScreens -> "Frontend Screens";
      };
  }
}
