package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionScoreEntry;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class HighscoreTestUtil {

  public static HighscoreChangeEvent createHighscoreChangeEvent(PlayerService playerService, Game game, double score, int pos, long serverId) {
    final DecimalFormat decimalFormat = new DecimalFormat("###,###,##0.00", DecimalFormatSymbols.getInstance(Locale.getDefault()));

    String output = decimalFormat.format(score);
    String output2 = decimalFormat.format(score+1000);

    Score oldScore = new Score(new Date(), game.getId(), "MFA", playerService.getPlayerForInitials(serverId, "MFA"), output, score, pos);
    Score newScore = new Score(new Date(), game.getId(), "MFA", playerService.getPlayerForInitials(serverId, "BOT"), output2, score + 1000, pos);
    return new HighscoreChangeEvent(game, null, null, oldScore, newScore);
  }

  public static DiscordHighscoreChangeEvent createDiscordHighscoreChangeEvent(Competition competition, String raw, Game game) {
    HighscoreMetadata metadata = new HighscoreMetadata();
    metadata.setDisplayName(game.getGameDisplayName());
    metadata.setRaw(raw);

    return new DiscordHighscoreChangeEvent(game, competition, metadata);
  }
}
