package de.mephisto.vpin.server.highscores.parsing.nvram.adapters;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class SkipFirstListScoreAdapter implements ScoreNvRamAdapter {

  private String name;

  public SkipFirstListScoreAdapter(String name) {
    this.name = name;
  }

  public SkipFirstListScoreAdapter() {

  }

  @Override
  public boolean isApplicable(@NonNull String nvRam, @NonNull List<String> lines) {
    return nvRam.equals(name);
  }

  @Override
  public String convert(@NonNull String nvRam, @NonNull List<String> lines) {
    int index = 0;
    for (String line : new ArrayList<>(lines)) {
      index++;
      if (line.trim().startsWith("HIGHEST SCORES")) {
        lines = lines.subList(index-1, lines.size());
        break;
      }
    }
    return String.join("\n", lines);
  }
}
