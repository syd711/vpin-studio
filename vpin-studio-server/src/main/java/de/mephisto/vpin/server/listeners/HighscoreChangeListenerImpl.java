package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.discord.DiscordOfflineChannelMessageFactory;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.HighscoreChangeListener;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.parsing.HighscoreParsingService;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HighscoreChangeListenerImpl implements InitializingBean, HighscoreChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreChangeListenerImpl.class);

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private DiscordService discordService;

  @Autowired
  private HighscoreParsingService highscoreParsingService;

  @Override
  public void highscoreUpdated(@NonNull Game game, @NonNull Highscore highscore) {
    //no used for diff calculation
  }

  @Override
  public synchronized void highscoreChanged(@NonNull HighscoreChangeEvent event) {
    Game game = event.getGame();

    String raw = event.getNewRaw();

    //find competition to notify about highscore updates
    List<Competition> competitionForGame = competitionService.getCompetitionForGame(game.getId());
    for (Competition competition : competitionForGame) {
      //we are only interested in active competition with a discord channel
      if (competition.getDiscordChannelId() > 0 && competition.isActive()) {
        long discordServerId = competition.getDiscordServerId();
        long discordChannelId = competition.getDiscordChannelId();

        if (competition.getType().equals(CompetitionType.OFFLINE.name())) {
          discordService.sendMessage(discordServerId, discordChannelId, DiscordOfflineChannelMessageFactory.createCompetitionHighscoreCreatedMessage(highscoreParsingService, competition, event, raw));
        }
      }
    }

    //send the default message if no competition updates was sent
    if (!event.isInitialScore() && !event.isEventReplay()) {
      if (!StringUtils.isEmpty(raw)) {
        LOG.info("Sending discord default notification for: {}", game.getGameDisplayName());
        SLOG.info("Sending discord default notification for: " + game.getGameDisplayName());
        discordService.sendDefaultHighscoreMessage(DiscordOfflineChannelMessageFactory.createHighscoreCreatedMessage(highscoreParsingService, event, raw));
      }
      else {
        LOG.warn("Skipped discord highscore emitting, raw score is empty.");
        SLOG.warn("Skipped discord highscore emitting, raw score is empty.");
      }
    }
    else {
      LOG.warn("Skipped discord highscore emitting, initial scores are ignored.");
      SLOG.warn("Skipped discord highscore emitting, initial scores are ignored.");
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    highscoreService.addHighscoreChangeListener(this);
  }
}
