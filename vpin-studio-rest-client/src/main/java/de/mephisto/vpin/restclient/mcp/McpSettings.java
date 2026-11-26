package de.mephisto.vpin.restclient.mcp;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

/**
 * Settings of the MCP (Model Context Protocol) endpoint that lets AI assistants like Claude query the tables
 * of this cabinet. The endpoint is served by the VPin Studio Server at {@code /mcp}.
 */
public class McpSettings extends JsonSettings {
  private boolean enabled = false;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.MCP_SETTINGS;
  }
}
