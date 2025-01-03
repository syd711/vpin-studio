package de.mephisto.vpin.server.highscores.parsing.nvram.adapters;

import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

public class SinglePlayerScoreAdapter implements ScoreNvRamAdapter {

  private String name;
  private int scoreLine;

  public SinglePlayerScoreAdapter(String name, int scoreLine) {
    this.name = name;
    this.scoreLine = scoreLine;
  }

  public SinglePlayerScoreAdapter() {

  }

  @Override
  public boolean isApplicable(@NonNull String nvRam, @NonNull List<String> lines) {
    return nvRam.equals(name) || lines.size() == 2;
  }

  @Override
  public String convert(@NonNull String nvRam, @NonNull List<String> lines) {
    StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");
    String score1 = lines.get(scoreLine);
    builder.append("#1");
    builder.append(" ");
    builder.append("???");
    builder.append("   ");
    builder.append(ScoreFormatUtil.cleanScore(score1));
    builder.append("\n");
    return builder.toString();
  }
}
