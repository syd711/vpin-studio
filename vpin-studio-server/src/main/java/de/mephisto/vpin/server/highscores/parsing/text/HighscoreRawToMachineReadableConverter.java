package de.mephisto.vpin.server.highscores.parsing.text;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class HighscoreRawToMachineReadableConverter {

  public static String convertToMachineReadable(List<String> lines) {
    if (lines.size() == 16) {
      lines = lines.subList(1, lines.size());
    }

    if (lines.size() == 26) {//UT99
      return convertLinesToRawScoreAlteringScore(0, 3, lines);
    }

    if (lines.size() >= 15) {
      return convertLinesToRawScoreLeadingScores(5, 5, lines);
    }

    if (lines.size() == 12) {
      return convertLinesToRawScoreLeadingScores(2, 5, lines);
    }

    if (lines.size() == 14) {
      return convertLinesToRawScoreLeadingScores(4, 5, lines);
    }

    if (lines.size() == 8) {
      StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");

      String score1 = lines.get(1);
      String score2 = lines.get(2);
      builder.append("#1");
      builder.append(" ");
      builder.append("???");
      builder.append("   ");
      builder.append(score2);
      builder.append("\n");

      builder.append("#2");
      builder.append(" ");
      builder.append("???");
      builder.append("   ");
      builder.append(score1);
      builder.append("\n");

      return builder.toString();
    }

    if (lines.size() == 7 && lines.get(1).indexOf(".:") == 1) {//spongebob
      StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");
      for (int i = 1; i < lines.size(); i++) {
        String line = lines.get(i);
        if (StringUtils.isEmpty(line.trim())) {
          continue;
        }

        List<String> scoreLineSegments = Arrays.asList(line.split(":"));
        String initials = scoreLineSegments.get(1);
        String score = scoreLineSegments.get(2);

        builder.append("#");
        builder.append(i);
        builder.append(" ");
        builder.append(initials);
        builder.append("   ");
        builder.append(score);
        builder.append("\n");
      }

      return builder.toString();
    }

    if (lines.size() == 2) {
      StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");

      String score1 = lines.get(1);
      builder.append("#1");
      builder.append(" ");
      builder.append("???");
      builder.append("   ");
      builder.append(score1.replaceAll("\\.", ""));
      builder.append("\n");

      builder.append("#2");
      builder.append(" ");
      builder.append("???");
      builder.append("   ");
      builder.append("0");
      builder.append("\n");

      builder.append("#3");
      builder.append(" ");
      builder.append("???");
      builder.append("   ");
      builder.append("0");
      builder.append("\n");

      return builder.toString();
    }

    return null;
  }


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
  private static String convertLinesToRawScoreLeadingScores(int start, int size, List<String> lines) {
    StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");

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
  private static String convertLinesToRawScoreAlteringScore(int start, int size, List<String> lines) {
    StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");

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
}
