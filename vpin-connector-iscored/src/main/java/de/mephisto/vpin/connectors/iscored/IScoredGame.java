package de.mephisto.vpin.connectors.iscored;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IScoredGame {
  private int id;
  private String name;
  private List<Score> scores = new ArrayList<>();
  private List<String> tags = new ArrayList<>();

  @JsonProperty("Hidden")
  private String hidden;

  public String getHidden() {
    return hidden;
  }

  public void setHidden(String hidden) {
    this.hidden = hidden;
  }

  @JsonIgnore
  public boolean isGameHidden() {
    return hidden != null && hidden.equalsIgnoreCase("TRUE");
  }

  public boolean isDisabled() {
    return tags.contains("vps:disabled");
  }

  public boolean isSingleScore() {
    return tags.contains("vps:singlescore");
  }

  public boolean isMultiScore() {
    return tags.contains("vps:multiscore");
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

  public boolean matches(String vpsTableId, String vpsVersionId) {
    List<String> tags = getTags();
    for (String tag : tags) {
      if (tag.contains(vpsTableId) && tag.contains(vpsVersionId)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return this.name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof IScoredGame)) return false;

    IScoredGame game = (IScoredGame) o;

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
