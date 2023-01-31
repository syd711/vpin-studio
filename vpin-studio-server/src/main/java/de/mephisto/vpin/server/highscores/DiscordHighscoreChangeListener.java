package de.mephisto.vpin.server.highscores;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface DiscordHighscoreChangeListener {

  void highscoreChanged(@NonNull DiscordHighscoreChangeEvent event);
}
