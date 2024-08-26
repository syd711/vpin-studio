package de.mephisto.vpin.server.highscores.parsing.text.adapters;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

abstract public class ScoreTextFileAdapterImpl implements ScoreTextFileAdapter {

  public static final String HIGHEST_SCORES = "HIGHEST SCORES";

  /**
   * E.g.
   * 7575
   * 7000
   * 6000
   * 5500
   * 5000
   * AAA
   * ZZZ
   * XXX
   * ABC
   * BBB
   */
  protected String convertAlteringScoreBlocks(int start, int size, List<String> lines) {
    StringBuilder builder = new StringBuilder(HIGHEST_SCORES + "\n");

    for (int index = 0; index < size; index++) {
      String score = lines.get(index + start);
      String initials = lines.get(index + start + size);

      builder.append("#");
      builder.append(index + 1);
      builder.append(" ");
      builder.append(initials);
      builder.append("   ");
      builder.append(score);
      builder.append("\n");
    }
    return builder.toString();
  }

  /**
   * E.g.
   * 10000000
   * VPX
   * 5000000
   * VPX
   * 1000000
   * VPX
   */
  protected String convertAlteringScoreLines(int start, int size, List<String> lines) {
    StringBuilder builder = new StringBuilder(HIGHEST_SCORES + "\n");

    int pos = 1;
    for (int index = 0; index < size * 2; index++) {
      if (index % 2 == 1) {
        continue;
      }

      String score = lines.get(index + start);
      String initials = lines.get(index + start + 1);

      builder.append("#");
      builder.append(pos);
      builder.append(" ");
      builder.append(initials);
      builder.append("   ");
      builder.append(score);
      builder.append("\n");

      pos++;
    }
    return builder.toString();
  }

  @Override
  public List<String> resetHighscore(@NotNull File file, @NotNull List<String> lines) {
    return lines;
  }
}
