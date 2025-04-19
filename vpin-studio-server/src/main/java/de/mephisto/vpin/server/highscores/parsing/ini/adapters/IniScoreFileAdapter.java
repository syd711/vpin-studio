package de.mephisto.vpin.server.highscores.parsing.ini.adapters;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;
import java.util.List;

public interface IniScoreFileAdapter {

  boolean isApplicable(@NonNull File file, @NonNull List<String> lines);

  String convert(@NonNull File file, @NonNull List<String> lines);

  List<String> resetHighscore(@NonNull File file, @NonNull List<String> lines, long score);
}
