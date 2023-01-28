package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.games.Game;

public interface HighscoreInitializedEvent {
  Game getGame();

  Highscore getHighscore();
}
