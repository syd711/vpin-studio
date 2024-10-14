package de.mephisto.vpin.server.highscores.parsing.text.adapters.customized;

import de.mephisto.vpin.server.highscores.parsing.text.adapters.ScoreTextFileAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * High Scores 2.1
 * 1.:EDY:1535780600
 * 2.:EDY:868583110
 * 3.:EDY:711932830
 * 4.:EDY:556150830
 * 5.:EDY:457438950
 * 6.:SDY:30000000
 */
public class SpongebobAdapter implements ScoreTextFileAdapter {
  @Override
  public boolean isApplicable(@NonNull File file, @NonNull List<String> lines) {
    return file.getName().equals("spongebob_hiscores.txt");
  }

  @Override
  public String convert(@NonNull File file, @NonNull List<String> lines) {
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

  @Override
  public List<String> resetHighscore(@NonNull File file, @NonNull List<String> lines) {
    List<String> newScoreText = new ArrayList<>();
    newScoreText.add(lines.get(0));
    newScoreText.add("1.:???:0");
    newScoreText.add("2.:???:0");
    newScoreText.add("3.:???:0");
    newScoreText.add("4.:???:0");
    newScoreText.add("5.:???:0");
    newScoreText.add("6.:???:0");
    return newScoreText;
  }
}
