package de.mephisto.vpin.restclient.frontend;

import java.util.ArrayList;
import java.util.List;

public class Frontend {
  private FrontendType frontendType;
  private String name;
  private String installationDirectory;
  private String adminExe;
  private String frontendExe;
  private String iconName;

  /**
   * Wether playfield media should be 180 rotated vs Popper standard layout
   */
  private boolean playfieldMediaInverted;

  /**
   * Potential conflict with external volume programs, e.g. PinVol
   */
  private boolean systemVolumeControlEnabled = false;

  private List<VPinScreen> supportedScreens = new ArrayList<>();
  private List<Integer> ignoredValidations = new ArrayList<>();

  private FieldLookups fieldLookups = new FieldLookups();

  public boolean isSystemVolumeControlEnabled() {
    return systemVolumeControlEnabled;
  }

  public void setSystemVolumeControlEnabled(boolean systemVolumeControlEnabled) {
    this.systemVolumeControlEnabled = systemVolumeControlEnabled;
  }

  public FieldLookups getFieldLookups() {
    return fieldLookups;
  }

  public void setFieldLookups(FieldLookups fieldLookups) {
    this.fieldLookups = fieldLookups;
  }

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public boolean isPlayfieldMediaInverted() {
    return playfieldMediaInverted;
  }

  public void setPlayfieldMediaInverted(boolean playfieldMediaInverted) {
    this.playfieldMediaInverted = playfieldMediaInverted;
  }
}
