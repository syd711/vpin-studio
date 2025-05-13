package de.mephisto.vpin.restclient.emulators;

import java.util.Objects;

public class LaunchConfiguration {
  private String label;
  private boolean launchViaFrontend;
  private String altExe;
  private String option;

  public LaunchConfiguration() {

  }

  public LaunchConfiguration(String label, boolean launchViaFrontend, String altExe, String option) {
    this.label = label;
    this.launchViaFrontend = launchViaFrontend;
    this.altExe = altExe;
    this.option = option;
  }

  public String getLabel() {
    return label;
  }

  public boolean isLaunchViaFrontend() {
    return launchViaFrontend;
  }

  public String getAltExe() {
    return altExe;
  }

  public String getOption() {
    return option;
  }

  @Override
  public String toString() {
    return label;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    LaunchConfiguration that = (LaunchConfiguration) o;
    return launchViaFrontend == that.launchViaFrontend && Objects.equals(label, that.label) && Objects.equals(altExe, that.altExe) && Objects.equals(option, that.option);
  }

  @Override
  public int hashCode() {
    return Objects.hash(label, launchViaFrontend, altExe, option);
  }
}
