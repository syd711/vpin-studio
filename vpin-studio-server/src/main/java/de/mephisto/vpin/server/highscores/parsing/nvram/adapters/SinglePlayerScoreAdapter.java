package de.mephisto.vpin.server.highscores.parsing.nvram.adapters;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
  public boolean isApplicable(@NotNull String nvRam, @NotNull List<String> lines) {
    return nvRam.equals(name) || lines.size() == 2;
  }

  @Override
  public String convert(@NotNull String nvRam, @NotNull List<String> lines) {
    StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");
    String score1 = lines.get(scoreLine);
    builder.append("#1");
    builder.append(" ");
    builder.append("???");
    builder.append("   ");
    builder.append(score1.replaceAll("\\.", ""));
    builder.append("\n");
    return builder.toString();
  }
}
