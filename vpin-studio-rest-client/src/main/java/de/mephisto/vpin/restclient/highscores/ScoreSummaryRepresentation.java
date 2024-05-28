package de.mephisto.vpin.restclient.highscores;

import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.Score;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.util.*;

public class ScoreSummaryRepresentation {
  private final static Logger LOG = LoggerFactory.getLogger(ScoreSummaryRepresentation.class);

  private Date createdAt;

  private String raw;

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
        s.setScore(ScoreFormatUtil.formatScore(gameRoomScore.getScore()));
        try {
          s.setNumericScore(Long.parseLong(gameRoomScore.getScore()));
        } catch (NumberFormatException e) {
          LOG.warn("iScored score formatting failed: " + e.getMessage());
        }

        PlayerRepresentation playerRepresentation = new PlayerRepresentation();
        playerRepresentation.setName(gameRoomScore.getName());
        s.setPlayer(playerRepresentation);
        summary.getScores().add(s);
      }
    }

    summary.getScores().sort((o1, o2) -> Double.compare(o2.getNumericScore(), o1.getNumericScore()));
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

  private List<ScoreRepresentation> scores;

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
}
