package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;

public class ServerSettings extends JsonSettings<ServerSettings> {
  private boolean vpsAutoApplyToPopper;
  private boolean vpxKeepFileNames;
  private boolean vpxKeepDisplayNames;

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
