package de.mephisto.vpin.server.highscores.parsing;

import de.mephisto.vpin.restclient.util.ScoreFormatUtil;

import java.util.ArrayList;
import java.util.List;

public class ScoreParsingSummary {

  private List<ScoreParsingEntry> scores = new ArrayList<>();

  public List<ScoreParsingEntry> getScores() {
    return scores;
  }

  public void setScores(List<ScoreParsingEntry> scores) {
    this.scores = scores;
  }

  public String toRaw() {
    StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");
    for (ScoreParsingEntry score : scores) {
      builder.append("#");
      builder.append(score.getPos());
      builder.append(" ");
      builder.append(score.getInitials());
      builder.append("   ");
      builder.append(ScoreFormatUtil.formatScore(String.valueOf(score.getScore())));
      builder.append("\n");
    }
    return builder.toString();
  }
}
