package de.mephisto.vpin.server.highscores.parsing.text.adapters.customized;

import de.mephisto.vpin.server.highscores.parsing.text.adapters.ScoreTextFileAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Miles=
 * Score=361
 */
public class Route66Adapter implements ScoreTextFileAdapter {
  @Override
  public boolean isApplicable(@NonNull File file, @NonNull List<String> lines) {
    return file.getName().equals("Route66.txt");
  }

  @Override
  public String convert(@NonNull File file, @NonNull List<String> lines) {
    StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");
    for (int i = 1; i < lines.size(); i++) {
      String line = lines.get(i);
      if (StringUtils.isEmpty(line.trim())) {
        continue;
      }

      if (!line.startsWith("Score=")) {
        continue;
      }


      List<String> scoreLineSegments = Arrays.asList(line.split("="));
      String initials = "???";
      String score = scoreLineSegments.get(1);

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

  @Override
  public List<String> resetHighscore(@NonNull File file, @NonNull List<String> lines) {
    List<String> newScoreText = new ArrayList<>();
    for (String line : lines) {
      if (!StringUtils.isEmpty(line) && line.startsWith("Score=")) {
        line = "Score=0";
      }
      newScoreText.add(line);
    }
    return newScoreText;
  }
}
