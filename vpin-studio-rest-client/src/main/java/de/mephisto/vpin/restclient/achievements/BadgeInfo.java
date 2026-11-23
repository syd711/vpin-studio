package de.mephisto.vpin.restclient.achievements;

/**
 * A badge with its state and progress for a player.
 */
public class BadgeInfo {
  public String id;
  public String name;
  public String description;
  /** Milestones, Prestige, Exploration, Playtime, Special, Rarity, Cloud &amp; Level */
  public String category;
  public boolean earned;
  public long current;
  public long target;
  /** progress text for locked badges, e.g. "306 / 500", "38 / 50 h", "best: 41 min" */
  public String progressText;
  /** how current/target are meant, used by the client to localize the progress: count, prestige, hours, best, best_minutes, flag, level */
  public String progressKind;
  public boolean selected;
}
