package de.mephisto.vpin.connectors.iscored;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IScoredGame {
  private String gameRoomUrl;
  private int id;
  private String name;
  private List<Score> scores = new ArrayList<>();
  private List<String> tags = new ArrayList<>();

  public String getGameRoomUrl() {
    return gameRoomUrl;
  }

  public void setGameRoomUrl(String gameRoomUrl) {
    this.gameRoomUrl = gameRoomUrl;
  }

  @JsonProperty("Hidden")
  private String hidden;

  @JsonProperty("Locked")
  private String locked;

  public String getLocked() {
    return locked;
  }

  public void setLocked(String locked) {
    this.locked = locked;
  }

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

  @JsonIgnore
  public boolean isGameLocked() {
    return locked != null && locked.equalsIgnoreCase("TRUE");
  }

  public boolean isVpsTagged() {
    List<String> tags = getTags();
    for (String tag : tags) {
      if (tag.startsWith("https://virtualpinballspreadsheet")) {
        return true;
      }
    }
    return false;
  }

  public boolean isVpsVersionTagged() {
    List<String> tags = getTags();
    for (String tag : tags) {
      if (tag.contains("#") && tag.startsWith("https://virtualpinballspreadsheet")) {
        return true;
      }
    }
    return false;
  }

  public boolean isAllVersionsEnabled() {
    if (!isVpsTagged()) {
      return false;
    }

    if (!isVpsVersionTagged()) {
      return true;
    }

    List<String> tags = getTags();
    for (String tag : tags) {
      if (tag.equalsIgnoreCase("vps:allversions")) {
        return true;
      }
    }

    return false;
  }

  public boolean isDisabled() {
    return tags.contains("vps:disabled");
  }

  public boolean isSingleScore() {
    for (String tag : tags) {
      if (tag.equalsIgnoreCase("vps:singlescore")) {
        return true;
      }
    }
    return false;
  }

  public boolean isMultiScore() {
    for (String tag : tags) {
      if (tag.equalsIgnoreCase("vps:multiscore")) {
        return true;
      }
    }
    return false;
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

  public boolean matches(@Nullable String vpsTableId, @Nullable String vpsVersionId) {
    if (vpsTableId == null) {
      return false;
    }

    List<String> tags = getTags();
    for (String tag : tags) {
      if (tag.contains(vpsTableId) && (vpsVersionId == null || isAllVersionsEnabled())) {
        return true;
      }

      if (tag.contains(vpsTableId) && vpsVersionId != null && tag.contains(vpsVersionId)) {
        return true;
      }
    }
    return false;
  }

  public String getVpsTableId() {
    List<String> tags = getTags();
    for (String tag : tags) {
      String vpsTableId = IScoredUtil.getQueryParams(tag, "game");
      if (vpsTableId != null && !vpsTableId.isEmpty()) {
        if (vpsTableId.contains("#")) {
          vpsTableId = vpsTableId.substring(0, vpsTableId.indexOf("#"));
        }
        return vpsTableId;
      }
    }
    return null;
  }

  public String getVpsTableVersionId() {
    List<String> tags = getTags();
    for (String tag : tags) {
      if (tag.contains("#")) {
        return tag.substring(tag.lastIndexOf("#") + +1);
      }
    }
    return null;
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
