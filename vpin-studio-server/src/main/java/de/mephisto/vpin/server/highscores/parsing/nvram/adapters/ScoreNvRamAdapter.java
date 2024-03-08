package de.mephisto.vpin.server.highscores.parsing.nvram.adapters;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.List;

public interface ScoreNvRamAdapter {

  boolean isApplicable(@NonNull String nvRam, @NonNull List<String> lines);

  String convert(@NonNull String nvRam, @NonNull List<String> lines, @Nullable String defaultInitials);
}
