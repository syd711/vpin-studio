package de.mephisto.vpin.server.highscores.parser;

import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.PlayerService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public interface HighscoreAdapter {

  @Nullable
  Score parse(@NotNull PlayerService playerService, @NonNull Date createdAt, @NotNull String line, int gameId, long serverId);
}
