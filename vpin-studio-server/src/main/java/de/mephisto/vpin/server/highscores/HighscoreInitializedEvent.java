package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;

public class HighscoreInitializedEvent {

  @NonNull
  private final Game game;
  @NonNull
  private final Highscore highscore;

  public HighscoreInitializedEvent(@NonNull Game game, @NonNull Highscore highscore) {
    this.game = game;
    this.highscore = highscore;
  }

  @NonNull
  public Game getGame() {
    return game;
  }

  @NonNull
  public Highscore getHighscore() {
    return highscore;
  }
}
