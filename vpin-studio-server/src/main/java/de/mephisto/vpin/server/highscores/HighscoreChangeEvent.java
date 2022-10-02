package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.jpa.Highscore;

public interface HighscoreChangeEvent {

  Game getGame();

  Highscore getOldHighscore();

  Highscore getNewHighscore();
}
