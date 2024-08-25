package de.mephisto.vpin.server.highscores.parsing.nvram.adapters;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Anonymous5PlayerScoreAdapter implements ScoreNvRamAdapter {

  private String name;

  public Anonymous5PlayerScoreAdapter(String name) {
    this.name = name;
  }

  public Anonymous5PlayerScoreAdapter() {

  }

  @Override
  public boolean isApplicable(@NotNull String nvRam, @NotNull List<String> lines) {
    return nvRam.equals(name);
  }

  @Override
  public String convert(@NotNull String nvRam, @NotNull List<String> lines) {
    int index = 0;
    int pos = 1;
    List<String> converted = new ArrayList<>();
    for (String line : new ArrayList<>(lines)) {
      if (index == 1 || index > 3) {
        if (line.length() > 12) {
          String score = line.substring(12).trim();
          converted.add(pos + ") ??? " + score);
          pos++;
        }
      }
      index++;
    }
    return String.join("\n", converted);
  }
}
