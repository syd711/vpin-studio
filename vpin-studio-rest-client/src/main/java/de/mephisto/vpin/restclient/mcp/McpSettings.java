package de.mephisto.vpin.restclient.mcp;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

/**
 * Settings of the MCP (Model Context Protocol) endpoint that lets AI assistants like Claude query the tables
 * of this cabinet. The endpoint is served by the VPin Studio Server at {@code /mcp}.
 * <p>
 * The tools that change the cabinet are only offered when the read-only mode is off, and then only for the groups
 * that are allowed here. Reading tools are always offered.
 */
public class McpSettings extends JsonSettings {
  private boolean enabled = false;

  /**
   * Only the tools that read data are offered. When switched off, the AI can also change the allowed groups.
   */
  private boolean readOnly = true;

  private boolean allowTables = true;
  private boolean allowCompetitions = true;
  private boolean allowPlaylists = true;
  private boolean allowMedia = true;
  private boolean allowSettings = false;
  private boolean allowSystem = false;

  public boolean isReadOnly() {
    return readOnly;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Tables: metadata, components, validations, highscore resets, launching and deleting.
   */
  public boolean isAllowTables() {
    return allowTables;
  }

  public void setAllowTables(boolean allowTables) {
    this.allowTables = allowTables;
  }

  /**
   * Competitions, iScored game rooms and players.
   */
  public boolean isAllowCompetitions() {
    return allowCompetitions;
  }

  public void setAllowCompetitions(boolean allowCompetitions) {
    this.allowCompetitions = allowCompetitions;
  }

  /**
   * Playlists and bulk tagging.
   */
  public boolean isAllowPlaylists() {
    return allowPlaylists;
  }

  public void setAllowPlaylists(boolean allowPlaylists) {
    this.allowPlaylists = allowPlaylists;
  }

  /**
   * Frontend media downloads and deletions, recordings and background jobs.
   */
  public boolean isAllowMedia() {
    return allowMedia;
  }

  public void setAllowMedia(boolean allowMedia) {
    this.allowMedia = allowMedia;
  }

  /**
   * The VPin Studio preferences. Secret values and the MCP settings themselves can never be changed by the AI.
   */
  public boolean isAllowSettings() {
    return allowSettings;
  }

  public void setAllowSettings(boolean allowSettings) {
    this.allowSettings = allowSettings;
  }

  /**
   * Frontend restart, backups, server restart and cabinet shutdown.
   */
  public boolean isAllowSystem() {
    return allowSystem;
  }

  public void setAllowSystem(boolean allowSystem) {
    this.allowSystem = allowSystem;
  }

  public boolean isAllowed(McpToolGroup group) {
    return switch (group) {
      case TABLES -> allowTables;
      case COMPETITIONS -> allowCompetitions;
      case PLAYLISTS -> allowPlaylists;
      case MEDIA -> allowMedia;
      case SETTINGS -> allowSettings;
      case SYSTEM -> allowSystem;
    };
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.MCP_SETTINGS;
  }
}
