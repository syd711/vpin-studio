package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

public class HighscoreChangeEvent {

  @NonNull
  private final Game game;
  @NonNull
  private final int scoreCount;
  @NonNull
  private final Score oldScore;
  @NonNull
  private final Score newScore;

  private boolean initialScore;

  public HighscoreChangeEvent(@NonNull Game game, @NonNull Score oldScore, @NonNull Score newScore, int scoreCount, boolean initialScore) {
    this.game = game;
    this.scoreCount = scoreCount;
    this.oldScore = oldScore;
    this.newScore = newScore;
    this.initialScore = initialScore;
  }

  public boolean isInitialScore() {
    return initialScore;
  }

  public int getScoreCount() {
    return scoreCount;
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
