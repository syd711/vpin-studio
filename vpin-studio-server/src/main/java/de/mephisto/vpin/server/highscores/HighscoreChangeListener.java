package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;

public interface HighscoreChangeListener {

  /**
   * Fired for every single change in the highscore list.
   * So when a game is parsed or multiple scores have created in one game, one event is fired for every score.
   *
   * @param event
   */
  void highscoreChanged(@NonNull HighscoreChangeEvent event);

  /**
   * Fired when a highscore has changed in general.
   *
   * @param highscore
   */
  void highscoreUpdated(@NonNull Game game, @NonNull Highscore highscore);
}
