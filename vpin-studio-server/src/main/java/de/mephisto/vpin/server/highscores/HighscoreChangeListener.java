package de.mephisto.vpin.server.highscores;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface HighscoreChangeListener {

  void highscoreChanged(@NonNull HighscoreChangeEvent event);
}
