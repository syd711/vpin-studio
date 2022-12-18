package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.games.Game;

public interface HighscoreChangeEvent {
  Game getGame();

  Highscore getOldHighscore();

  Highscore getNewHighscore();

  Score getOldScore();

  Score getNewScore();
}
