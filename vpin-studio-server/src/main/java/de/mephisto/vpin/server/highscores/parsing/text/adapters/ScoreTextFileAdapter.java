package de.mephisto.vpin.server.highscores.parsing.text.adapters;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.File;
import java.util.List;

public interface ScoreTextFileAdapter {

  boolean isApplicable(@NonNull File file, @NonNull List<String> lines);

  String convert(@NonNull File file, @NonNull List<String> lines);

  List<String> resetHighscore(@NonNull File file, @NonNull List<String> lines);
}
