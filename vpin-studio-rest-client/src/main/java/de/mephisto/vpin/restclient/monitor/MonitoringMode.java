package de.mephisto.vpin.restclient.monitor;

public enum MonitoringMode {
  frontendScreens, monitors, streamingView;


  @Override
  public String toString() {
      return switch (this) {
          case monitors -> "All Monitors";
          case frontendScreens -> "Frontend Screens";
          case streamingView -> "Streaming View";
      };
  }
}
