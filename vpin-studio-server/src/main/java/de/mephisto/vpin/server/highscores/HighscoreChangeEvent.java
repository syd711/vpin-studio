package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

public class HighscoreChangeEvent {

  @NonNull
  private final Game game;
  @NonNull
  private final List<Score> oldScores;
  @NonNull
  private final List<Score> newScores;
  @NonNull
  private final Score oldScore;
  @NonNull
  private final Score newScore;

  public HighscoreChangeEvent(@NonNull Game game, @NonNull List<Score> oldScores, @NonNull List<Score> newScores, @NonNull Score oldScore, @NonNull Score newScore) {
    this.game = game;
    this.oldScores = oldScores;
    this.newScores = newScores;
    this.oldScore = oldScore;
    this.newScore = newScore;
  }

  @NonNull
  public List<Score> getOldScores() {
    return oldScores;
  }

  @NonNull
  public List<Score> getNewScores() {
    return newScores;
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
