package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;

public class HighscoreChangeEvent {

  @NonNull
  private final Game game;
  @NonNull
  private final Highscore oldHighscore;
  @NonNull
  private final Highscore newHighscore;
  @NonNull
  private final Score oldScore;
  @NonNull
  private final Score newScore;

  public HighscoreChangeEvent(@NonNull Game game, @NonNull Highscore oldHighscore, @NonNull Highscore newHighscore, @NonNull Score oldScore, @NonNull Score newScore) {
    this.game = game;
    this.oldHighscore = oldHighscore;
    this.newHighscore = newHighscore;
    this.oldScore = oldScore;
    this.newScore = newScore;
  }

  @NonNull
  public Game getGame() {
    return game;
  }

  @NonNull
  public Highscore getOldHighscore() {
    return oldHighscore;
  }

  @NonNull
  public Highscore getNewHighscore() {
    return newHighscore;
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
