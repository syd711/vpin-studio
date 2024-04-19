package de.mephisto.vpin.connectors.iscored;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Game {
  private int id;
  private String name;
  private List<Score> scores = new ArrayList<>();
  private List<String> tags = new ArrayList<>();

  public boolean isDisabled() {
    return tags.contains("vps:disabled");
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Score> getScores() {
    return scores;
  }

  public void setScores(List<Score> scores) {
    this.scores = scores;
  }

  @Override
  public String toString() {
    return this.name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Game)) return false;

    Game game = (Game) o;

    if (id != game.id) return false;
    return Objects.equals(name, game.name);
  }

  @Override
  public int hashCode() {
    int result = id;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    return result;
  }
}
