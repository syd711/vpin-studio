package de.mephisto.vpin.restclient.achievements;

/**
 * One row of the level table.
 */
public class LevelDefinition {
  public int level;
  public String name;
  public int from;

  public LevelDefinition() {
  }

  public LevelDefinition(int level, String name, int from) {
    this.level = level;
    this.name = name;
    this.from = from;
  }
}
