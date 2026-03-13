package de.mephisto.vpin.restclient.highscores;

import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.connectors.iscored.Score;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ScoreSummaryRepresentation {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private Date createdAt;

  private String raw;

  private List<ScoreRepresentation> scores = new ArrayList<>();

  @NonNull
  public static ScoreSummaryRepresentation forGameRoom(GameRoom gameRoom, String vpsTableId, String vpsVersionId) {
    ScoreSummaryRepresentation summary = new ScoreSummaryRepresentation();
    summary.setScores(new ArrayList<>());
    IScoredGame gameByVps = gameRoom.getGameByVps(vpsTableId, vpsVersionId);
    if (gameByVps != null) {
      List<Score> gameRoomScores = gameByVps.getScores();
      for (Score gameRoomScore : gameRoomScores) {
        ScoreRepresentation s = new ScoreRepresentation();
        s.setCreatedAt(gameRoomScore.getDate());
        s.setScore(gameRoomScore.getScore());

        PlayerRepresentation playerRepresentation = new PlayerRepresentation();
        playerRepresentation.setName(gameRoomScore.getName());
        s.setPlayer(playerRepresentation);
        summary.getScores().add(s);
      }
    }

    summary.getScores().sort((o1, o2) -> Long.compare(o2.getScore(), o1.getScore()));
    for (int i = 1; i < summary.getScores().size(); i++) {
      summary.getScores().get(i - 1).setPosition(i);
    }

    return summary;
  }

  public String getRaw() {
    return raw;
  }

  public void setRaw(String raw) {
    this.raw = raw;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public List<ScoreRepresentation> getScores() {
    return scores;
  }

  public void setScores(List<ScoreRepresentation> scores) {
    this.scores = scores;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    ScoreSummaryRepresentation that = (ScoreSummaryRepresentation) o;
    return Objects.equals(createdAt, that.createdAt) && Objects.equals(raw, that.raw) && Objects.equals(scores, that.scores);
  }

  @Override
  public int hashCode() {
    return Objects.hash(createdAt, raw, scores);
  }

  @Override
  public String toString() {
    if(scores != null) {
      return "Score Summary (" + this.scores.size() + " scores)";
    }
    return "Score Summary (0 scores)";
  }
}
