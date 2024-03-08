package de.mephisto.vpin.server.highscores.parsing.text.adapters;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class SinglePlayerScoreAdapter extends ScoreTextFileAdapterImpl {

  private String name;
  private int scoreLine;

  public SinglePlayerScoreAdapter(String name, int scoreLine) {
    this.name = name;
    this.scoreLine = scoreLine;
  }

  public SinglePlayerScoreAdapter() {

  }

  @Override
  public boolean isApplicable(@NotNull File file, @NotNull List<String> lines) {
    if (name != null) {
      return file.getName().equals(name);
    }
    return lines.size() == 1;
  }

  @Override
  public String convert(@NotNull File file, @NotNull List<String> lines, @Nullable String defaultInitials) {
    if (StringUtils.isEmpty(defaultInitials)) {
      defaultInitials = "???";
    }

    StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");
    String score1 = lines.get(scoreLine);
    builder.append("#1");
    builder.append(" ");
    builder.append(defaultInitials);
    builder.append("   ");
    builder.append(score1.replaceAll("\\.", ""));
    builder.append("\n");
    return builder.toString();
  }
}
