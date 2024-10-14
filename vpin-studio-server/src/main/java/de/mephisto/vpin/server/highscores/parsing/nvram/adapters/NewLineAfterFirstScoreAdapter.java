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
    int index = 0;
    lines = new ArrayList<>(lines);
    lines.remove(2);

    //pos index contains a ","
    lines = lines.stream().map(l -> l.replaceAll(",", ")")).collect(Collectors.toList());

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
