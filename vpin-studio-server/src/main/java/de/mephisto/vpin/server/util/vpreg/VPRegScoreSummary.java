package de.mephisto.vpin.server.util.vpreg;

import de.mephisto.vpin.server.util.ScoreHelper;

import java.util.ArrayList;
import java.util.List;

public class VPRegScoreSummary {

  private List<VPRegScoreEntry> scores = new ArrayList<>();

  public List<VPRegScoreEntry> getScores() {
    return scores;
  }

  public void setScores(List<VPRegScoreEntry> scores) {
    this.scores = scores;
  }

  public String toRaw() {
    StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");
    for (VPRegScoreEntry score : scores) {
      builder.append("#");
      builder.append(score.getPos());
      builder.append(" ");
      builder.append(score.getInitials());
      builder.append("   ");
      builder.append(ScoreHelper.formatScore(String.valueOf(score.getScore())));
      builder.append("\n");
    }
    return builder.toString();
  }
}
