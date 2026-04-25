package de.mephisto.vpin.server.highscores.parsing.nvram.adapters;

import org.jspecify.annotations.NonNull;

import java.util.List;

public interface ScoreNvRamAdapter {

  boolean isApplicable(@NonNull String nvRam, @NonNull List<String> lines);

  String convert(@NonNull String nvRam, @NonNull List<String> lines);
}
