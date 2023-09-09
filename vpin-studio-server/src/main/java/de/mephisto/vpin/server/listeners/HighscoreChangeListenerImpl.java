package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.JoinMode;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.SubscriptionInfo;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.discord.DiscordCompetitionService;
import de.mephisto.vpin.server.discord.DiscordOfflineChannelMessageFactory;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.HighscoreChangeListener;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
  private PreferencesService preferencesService;

  @Autowired
  private DiscordCompetitionService discordCompetitionService;

  @Override
  public synchronized void highscoreChanged(@NotNull HighscoreChangeEvent event) {
    Game game = event.getGame();

    //update channel subscriptions
    List<Competition> competitionForRom = competitionService.getSubscriptions(game.getRom());
    Optional<Competition> newCompetition = runSubscriptionChannelsCheck(game, competitionForRom);

    boolean messageSent = false;
    for (Competition competition : competitionForRom) {
      //subscriptions don't need competition data, because we stick with a simple ROM name check.
      discordCompetitionService.runDiscordServerUpdate(event.getGame(), event.getNewScore(), competition, null);
      messageSent = true;
    }

    String raw = null;
    Optional<Highscore> highscore = highscoreService.getOrCreateHighscore(game);
    if (highscore.isPresent()) {
      raw = highscore.get().getRaw();
    }

    //find competition to notify about highscore updates
    List<Competition> competitionForGame = competitionService.getCompetitionForGame(game.getId());
    for (Competition competition : competitionForGame) {
      //we are only interested in active competition with a discord channel
      if (competition.getDiscordChannelId() > 0 && competition.isActive()) {
        long discordServerId = competition.getDiscordServerId();
        long discordChannelId = competition.getDiscordChannelId();

        if (competition.getType().equals(CompetitionType.OFFLINE.name())) {
          discordService.sendMessage(discordServerId, discordChannelId, DiscordOfflineChannelMessageFactory.createCompetitionHighscoreCreatedMessage(competition, event, raw));
          messageSent = true;
        }
        else if (competition.getType().equals(CompetitionType.DISCORD.name())) {
          if (discordService.isCompetitionActive(discordServerId, discordChannelId, competition.getUuid())) {
            DiscordCompetitionData competitionData = discordService.getCompetitionData(discordServerId, discordChannelId);
            discordCompetitionService.runDiscordServerUpdate(event.getGame(), event.getNewScore(), competition, competitionData);
          }
          else {
            LOG.warn("Skipping Discord highscore update for " + competition.getName() + ", no or invalid competition data found.");
            competitionService.finishCompetition(competition);
          }
          messageSent = true;
        }
      }
    }

    //send the default message if no competition updates was sent
    if (!messageSent && !event.isInitialScore()) {
      LOG.info("No competition found for " + game + ", sending default notification.");
      if (!StringUtils.isEmpty(raw)) {
        discordService.sendDefaultHighscoreMessage(DiscordOfflineChannelMessageFactory.createHighscoreCreatedMessage(event, raw));
      }
    }
  }

  private Optional<Competition> runSubscriptionChannelsCheck(Game game, List<Competition> competitionForRom) {
    boolean dynamicSubscriptionsEnabled = (boolean) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_DYNAMIC_SUBSCRIPTIONS);
    String defaultDiscordServerId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_GUILD_ID);

    if (dynamicSubscriptionsEnabled) {
      if (StringUtils.isEmpty(defaultDiscordServerId) && !competitionForRom.isEmpty()) {
        LOG.info("The default Discord server id is not set, but subscriptions have been created and dynamic subscriptions are enabled. You may want to configure a default server so that subscription are generated for your server too.");
      }
      else {
        boolean subscriptionUpdated = competitionForRom.stream().anyMatch(c -> c.getDiscordServerId() == Long.parseLong(defaultDiscordServerId));

        //if the dynamic subscriptions are enabled and no competition has been created yet, we must create a new one.
        if (!subscriptionUpdated) {
          Competition competition = new Competition();
          competition.setType(CompetitionType.SUBSCRIPTION.name());
          competition.setHighscoreReset(false);
          competition.setDiscordServerId(Long.parseLong(defaultDiscordServerId));
          competition.setOwner(String.valueOf(this.discordService.getBotId()));
          competition.setRom(game.getRom());
          competition.setUuid(UUID.randomUUID().toString());
          competition.setGameId(game.getId());
          competition.setScoreLimit(SubscriptionInfo.DEFAULT_SCORE_LIMIT);
          competition.setName(game.getGameDisplayName());
          competition.setJoinMode(JoinMode.ROM_ONLY.name());
          return Optional.of(competitionService.save(competition));
        }
      }
    }
    else {
      LOG.info("Skipped dynamic subscription updates, because they are not enabled.");
    }
    return Optional.empty();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    highscoreService.addHighscoreChangeListener(this);
  }
}
