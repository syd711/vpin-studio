package de.mephisto.vpin.server.highscores.parsing.text.adapters.customized;

import de.mephisto.vpin.server.highscores.parsing.text.adapters.ScoreTextFileAdapter;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class SpongebobAdapter implements ScoreTextFileAdapter {
  @Override
  public boolean isApplicable(@NotNull File file, @NotNull List<String> lines) {
    return file.getName().equals("spongebob_hiscores.txt");
  }

  @Override
  public String convert(@NotNull File file, @NotNull List<String> lines, @Nullable String defaultInitials) {
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
}
