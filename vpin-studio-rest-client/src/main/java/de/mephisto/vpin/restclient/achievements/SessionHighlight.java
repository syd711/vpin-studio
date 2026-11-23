package de.mephisto.vpin.restclient.achievements;

/**
 * A highlight of a played game, derived from the audit deltas (e.g. "Multiball – 2").
 */
public class SessionHighlight {
  /** rule key, e.g. "multiball", used for the translated label (achievements.highlight.&lt;key&gt;) */
  public String key;
  /** Power, Precision or Fun */
  public String category;
  /** English label */
  public String label;
  public long value;
  /** a yes/no highlight, the value is not shown */
  public boolean flag;
}
