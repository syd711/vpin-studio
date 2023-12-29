package de.mephisto.vpin.connectors.iscored;

import java.util.ArrayList;
import java.util.List;

public class Game {
  private int id;
  private String name;
  private List<Score> scores = new ArrayList<>();

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
}
