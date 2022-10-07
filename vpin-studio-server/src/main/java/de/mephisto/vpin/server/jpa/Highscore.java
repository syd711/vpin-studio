package de.mephisto.vpin.server.jpa;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Highscores")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Highscore {

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  @CreatedDate
  private Date createdAt;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  @LastModifiedDate
  private Date updatedAt;

  @Id
  @Column(name = "pupId", nullable = false)
  private int pupId;

  private String initials1;
  private String initials2;
  private String initials3;
  private String score1;
  private String score2;
  private String score3;

  private String displayName;

  private String raw;

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public int getPupId() {
    return pupId;
  }

  public void setPupId(int pupId) {
    this.pupId = pupId;
  }

  public String getInitials1() {
    return initials1;
  }

  public void setInitials1(String initials1) {
    this.initials1 = initials1;
  }

  public String getInitials2() {
    return initials2;
  }

  public void setInitials2(String initials2) {
    this.initials2 = initials2;
  }

  public String getInitials3() {
    return initials3;
  }

  public void setInitials3(String initials3) {
    this.initials3 = initials3;
  }

  public String getScore1() {
    return score1;
  }

  public void setScore1(String score1) {
    this.score1 = score1;
  }

  public String getScore2() {
    return score2;
  }

  public void setScore2(String score2) {
    this.score2 = score2;
  }

  public String getScore3() {
    return score3;
  }

  public void setScore3(String score3) {
    this.score3 = score3;
  }

  public String getRaw() {
    return raw;
  }

  public void setRaw(String raw) {
    this.raw = raw;
  }

  public List<Score> toScores() {
    List<Score> result = new ArrayList<>();
    if (!StringUtils.isEmpty(this.score1)) {
      result.add(new Score(initials1, score1, 1));
    }
    if (!StringUtils.isEmpty(this.score2)) {
      result.add(new Score(initials2, score2, 2));
    }
    if (!StringUtils.isEmpty(this.score3)) {
      result.add(new Score(initials3, score3, 3));
    }
    return result;
  }

  /**
   * The score has changed, which only happens when a new score is created.
   * So we only have to check if the actual score changed.
   *
   * @param h the Highscore to compare
   * @return true if the compared highscore is higher than this one.
   */
  public boolean containsHigherScoreThan(Highscore h) {
    return !String.valueOf(score1).equals(String.valueOf(score1)) ||
        !String.valueOf(score2).equals(String.valueOf(score2)) ||
        !String.valueOf(score3).equals(String.valueOf(score3));
  }

  public static Highscore forGame(@NonNull Game game, @Nullable String rawValue) {
    Highscore highscore = new Highscore();
    highscore.setRaw(rawValue);
    highscore.setPupId(game.getId());
    highscore.setCreatedAt(new Date());
    highscore.setUpdatedAt(new Date());
    highscore.setDisplayName(game.getGameDisplayName());
    return highscore;
  }
}
