package de.mephisto.vpin.server.highscores.parsing.nvram.adapters;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class FixTitleScoreAdapter implements ScoreNvRamAdapter {

  private String allow;
  private String deny;
  private String name;

  public FixTitleScoreAdapter(String name, String allow, String deny) {
    this.name = name;
    this.allow = allow;
    this.deny = deny;
  }

  public FixTitleScoreAdapter() {
  }

  @Override
  public boolean isApplicable(@NonNull String nvRam, @NonNull List<String> lines) {
    return nvRam.equals(name);
  }

  @Override
  public String convert(@NonNull String nvRam, @NonNull List<String> lines) {
    List<String> converted = new ArrayList<>();
    boolean foundTitle = false;
    for (String line : new ArrayList<>(lines)) {
      if (line.indexOf(")") == 1 && !foundTitle) {
        continue;
      }

      if(line.equals(deny)) {
        continue;
      }

      if (!foundTitle && line.equals(allow)) {
        foundTitle = true;
      }

      converted.add(line);
    }
    return String.join("\n", converted);
  }
}
