package de.mephisto.vpin.restclient.frontend;

import java.util.ArrayList;
import java.util.List;

public class Frontend {
  private FrontendType frontendType;
  private String installationDirectory;
  private String adminExe;
  private String frontendExe;
  private String iconName;
  private List<VPinScreen> supportedScreens = new ArrayList<>();
  private List<Integer> ignoredValidations = new ArrayList<>();

  public List<Integer> getIgnoredValidations() {
    return ignoredValidations;
  }

  public void setIgnoredValidations(List<Integer> ignoredValidations) {
    this.ignoredValidations = ignoredValidations;
  }

  public List<VPinScreen> getSupportedScreens() {
    return supportedScreens;
  }

  public void setSupportedScreens(List<VPinScreen> supportedScreens) {
    this.supportedScreens = supportedScreens;
  }

  public FrontendType getFrontendType() {
    return frontendType;
  }

  public void setFrontendType(FrontendType frontendType) {
    this.frontendType = frontendType;
  }

  public String getInstallationDirectory() {
    return installationDirectory;
  }

  public void setInstallationDirectory(String installationDirectory) {
    this.installationDirectory = installationDirectory;
  }

  public String getAdminExe() {
    return adminExe;
  }

  public void setAdminExe(String adminExe) {
    this.adminExe = adminExe;
  }

  public String getFrontendExe() {
    return frontendExe;
  }

  public void setFrontendExe(String frontendExe) {
    this.frontendExe = frontendExe;
  }

  public String getIconName() {
    return iconName;
  }

  public void setIconName(String iconName) {
    this.iconName = iconName;
  }
}
