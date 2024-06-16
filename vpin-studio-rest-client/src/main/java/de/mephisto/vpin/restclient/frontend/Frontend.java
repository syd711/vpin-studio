package de.mephisto.vpin.restclient.frontend;

public class Frontend {
  private FrontendType frontendType;
  private String installationDirectory;
  private String adminExe;
  private String frontendExe;
  private String iconName;

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
