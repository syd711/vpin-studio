package de.mephisto.vpin.server.highscores;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class HighscoreRawToMachineReadableConverter {

  public static String convertToMachineReadable(List<String> lines) {
    if (lines.size() == 16) {
      lines = lines.subList(1, lines.size());
    }

    if (lines.size() >= 15) {
      StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");

      int index = 5;
      for (int i = 1; i < 6; i++) {
        String score = lines.get(index);
        String initials = lines.get(index + 5);

        builder.append("#");
        builder.append(i);
        builder.append(" ");
        builder.append(initials);
        builder.append("   ");
        builder.append(score);
        builder.append("\n");

        index++;
      }
      return builder.toString();
    }

    if (lines.size() == 12) {
      StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");

      for (int index = 0; index < 5; index++) {
        String score = lines.get(index + 2);
        String initials = lines.get(index + 2 + 5);

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

    if (lines.size() == 14) {
      StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");

      for (int index = 0; index < 5; index++) {
        String score   = lines.get(index + 4);
        String initials = lines.get(index + 4 + 5);

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

    if (lines.size() == 7 && lines.get(1).indexOf(".:") == 1) {
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
}
