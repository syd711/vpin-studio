package de.mephisto.vpin.restclient.descriptors;

public class ArchiveDownloadAndInstallDescriptor extends ArchiveInstallDescriptor {
  private boolean install;

  public boolean isInstall() {
    return install;
  }

  public void setInstall(boolean install) {
    this.install = install;
  }
}
