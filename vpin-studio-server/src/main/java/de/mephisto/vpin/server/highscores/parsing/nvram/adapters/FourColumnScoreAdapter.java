package de.mephisto.vpin.server.highscores.parsing.nvram.adapters;

import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class FourColumnScoreAdapter implements ScoreNvRamAdapter {

  private String name;

  public FourColumnScoreAdapter(String name) {
    this.name = name;
  }

  public FourColumnScoreAdapter() {

  }

  @Override
  public boolean isApplicable(@NonNull String nvRam, @NonNull List<String> lines) {
    return nvRam.equals(name);
  }

  @Override
  public String convert(@NonNull String nvRam, @NonNull List<String> lines) {
    StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");

    for (int i = 1; i < lines.size(); i++) {
      String line = lines.get(i);
      String[] s = line.split(" ");

      builder.append("#" + i);
      builder.append(" ");

      String initials = s[2];
      if (i >= 10) {
        initials = s[1];
      }
      builder.append(initials);
      builder.append("   ");

      int subIndex = 3;
      String score = s[subIndex];
      while (StringUtils.isEmpty(score) && subIndex < 10) {
        subIndex++;
        score = s[subIndex];
      }
      score = ScoreFormatUtil.cleanScore(score);
      builder.append(score);
      builder.append("\n");
    }
    return builder.toString();
  }
}
