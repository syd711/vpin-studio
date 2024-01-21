package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;

public class ServerSettings extends JsonSettings {
  private boolean vpsAutoApplyToPopper;
  private boolean vpxKeepFileNames;
  private boolean vpxKeepDisplayNames;
  private boolean launchPopperOnExit = true;

  public boolean isLaunchPopperOnExit() {
    return launchPopperOnExit;
  }

  public void setLaunchPopperOnExit(boolean launchPopperOnExit) {
    this.launchPopperOnExit = launchPopperOnExit;
  }

  public boolean isVpsAutoApplyToPopper() {
    return vpsAutoApplyToPopper;
  }

  public void setVpsAutoApplyToPopper(boolean vpsAutoApplyToPopper) {
    this.vpsAutoApplyToPopper = vpsAutoApplyToPopper;
  }

  public boolean isVpxKeepFileNames() {
    return vpxKeepFileNames;
  }

  public void setVpxKeepFileNames(boolean vpxKeepFileNames) {
    this.vpxKeepFileNames = vpxKeepFileNames;
  }

  public boolean isVpxKeepDisplayNames() {
    return vpxKeepDisplayNames;
  }

  public void setVpxKeepDisplayNames(boolean vpxKeepDisplayNames) {
    this.vpxKeepDisplayNames = vpxKeepDisplayNames;
  }
}
