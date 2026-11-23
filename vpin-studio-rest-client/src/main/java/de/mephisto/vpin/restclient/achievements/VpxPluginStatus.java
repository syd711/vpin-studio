package de.mephisto.vpin.restclient.achievements;

/**
 * State of the VPX plugins (score-server + patched PinMAME plugin) used for the live tracking of the achievements.
 */
public class VpxPluginStatus {
  public static final String DISABLED = "disabled";
  public static final String NOT_SUPPORTED = "not_supported";
  public static final String VPX_NOT_FOUND = "vpx_not_found";
  public static final String VPX_OLD = "vpx_old";
  public static final String NO_BUILD = "no_build";
  public static final String REBUILD_NEEDED = "rebuild_needed";
  public static final String BUSY = "busy";
  public static final String FAILED = "failed";
  public static final String READY = "ready";

  /** one of the constants above */
  public String state = DISABLED;
  /** the plugins are installed and enabled */
  public boolean ready;
  /** the score-server plugin is connected right now (a table is played) */
  public boolean connected;
  /** English details for the states failed and rebuild_needed */
  public String detail;
  public String vpxVersion;
  public String vpxFolder;
  /** tag of the installed plugin build, e.g. vpx-plugins-10.8.1-af26b2d-r3 */
  public String installedTag;
}
