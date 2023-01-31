package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;

public class DiscordHighscoreChangeEvent {

  private final Game game;
  private final Competition competition;
  private final HighscoreMetadata data;

  public DiscordHighscoreChangeEvent(@NonNull Game game, @NonNull Competition competition, @NonNull HighscoreMetadata data) {
    this.game = game;
    this.competition = competition;
    this.data = data;
  }

  public Game getGame() {
    return game;
  }

  public Competition getCompetition() {
    return competition;
  }

  public HighscoreMetadata getHighscoreMetaData() {
    return data;
  }
}
