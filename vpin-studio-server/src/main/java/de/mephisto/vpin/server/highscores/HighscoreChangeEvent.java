package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;

public class HighscoreChangeEvent {

  @NonNull
  private final Game game;
  @NonNull
  private final Score oldScore;
  @NonNull
  private final Score newScore;

  public HighscoreChangeEvent(@NonNull Game game, @NonNull Score oldScore, @NonNull Score newScore) {
    this.game = game;
    this.oldScore = oldScore;
    this.newScore = newScore;
  }

  @NonNull
  public Game getGame() {
    return game;
  }

  @NonNull
  public Score getOldScore() {
    return oldScore;
  }

  @NonNull
  public Score getNewScore() {
    return newScore;
  }
}
