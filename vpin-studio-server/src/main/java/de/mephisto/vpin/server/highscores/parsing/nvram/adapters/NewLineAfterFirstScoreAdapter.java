package de.mephisto.vpin.server.highscores.parsing.nvram.adapters;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NewLineAfterFirstScoreAdapter implements ScoreNvRamAdapter {

  private String name;

  public NewLineAfterFirstScoreAdapter(String name) {
    this.name = name;
  }

  public NewLineAfterFirstScoreAdapter() {

  }

  @Override
  public boolean isApplicable(@NonNull String nvRam, @NonNull List<String> lines) {
    return nvRam.equals("kiko_a10.nv");
  }

  @Override
  public String convert(@NonNull String nvRam, @NonNull List<String> lines) {
    //pos index contains a ","
    lines = lines.stream().map(l -> l.replaceAll(",", ")")).collect(Collectors.toList());
    List<String> updatedLines = new ArrayList<>();
    for (String line : lines) {
      if (line.trim().contains("HIGHEST") || line.trim().contains("SCORES")) {
        continue;
      }
      updatedLines.add(line);
    }
    return String.join("\n", updatedLines);
  }
}
