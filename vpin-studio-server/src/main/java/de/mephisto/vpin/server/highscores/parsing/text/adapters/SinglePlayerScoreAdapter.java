package de.mephisto.vpin.server.highscores.parsing.text.adapters;

import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
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
  public String convert(@NotNull File file, @NotNull List<String> lines) {
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

  @Override
  public List<String> resetHighscore(@NotNull File file, @NotNull List<String> lines) {
    List<String> newScoreText = new ArrayList<>();
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (i == scoreLine) {
        newScoreText.add("0");
        continue;
      }
      newScoreText.add(line);
    }
    return newScoreText;
  }
}
