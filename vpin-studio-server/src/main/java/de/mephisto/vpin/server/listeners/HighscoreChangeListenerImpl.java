package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.JoinMode;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionScoreValidator;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.discord.*;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.*;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
  private HighscoreParser highscoreParser;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private DiscordCompetitionService discordCompetitionService;

  @Autowired
  private DiscordSubscriptionMessageFactory discordSubscriptionMessageFactory;

  @Override
  public synchronized void highscoreChanged(@NotNull HighscoreChangeEvent event) {
    Game game = event.getGame();

    String raw = null;
    Optional<Highscore> highscore = highscoreService.getOrCreateHighscore(game);
    if (highscore.isPresent()) {
      raw = highscore.get().getRaw();
    }

    //update channel subscriptions
    List<Competition> competitionForRom = competitionService.getSubscriptions(game.getRom());
    for (Competition competition : competitionForRom) {
      discordSubscriptionHighscoreChanged(event, competition);
    }

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
          competition.setName(game.getGameDisplayName());
          competition.setJoinMode(JoinMode.ROM_ONLY.name());
          competitionService.save(competition);
        }
      }
    }
    else {
      LOG.info("Skipped dynamic subscription updates, because they are not enabled.");
    }


    //find competition to notify about highscore updates
    List<Competition> competitionForGame = competitionService.getCompetitionForGame(game.getId());
    boolean messageSent = false;
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
      discordService.sendDefaultHighscoreMessage(DiscordOfflineChannelMessageFactory.createHighscoreCreatedMessage(event, raw));
    }
  }

  /**
   * Up til now we only THAT the score has changed, but not how.
   *
   * @param event       the highscore change event with the updated score
   * @param competition the online competition the score is for
   */
  private void discordSubscriptionHighscoreChanged(@NotNull HighscoreChangeEvent event, @NonNull Competition competition) {
    Game game = event.getGame();
    Score newScore = event.getNewScore();

    long discordServerId = competition.getDiscordServerId();
    long discordChannelId = competition.getDiscordChannelId();

    LOG.info("****** Processing Subscription Change Event for " + game.getGameDisplayName() + " *********");
    LOG.info("The new score: " + newScore);
    if (newScore.getPlayerInitials().contains("?")) {
      LOG.info("Highscore update has been skipped, initials with '?' are filtered.");
    }
    else {
      ScoreSummary scoreSummary = discordService.getScoreSummary(highscoreParser, competition.getUuid(), discordServerId, discordChannelId);
      if (scoreSummary.getScores().isEmpty()) {
        Optional<Highscore> highscore = highscoreService.getOrCreateHighscore(game);
        if (highscore.isPresent()) {
          Highscore hs = highscore.get();
          List<Score> scores = highscoreParser.parseScores(hs.getCreatedAt(), hs.getRaw(), competition.getGameId(), discordServerId);

          LOG.info("Emitting initial highscore message for " + competition);
          String msg = discordSubscriptionMessageFactory.createFirstSubscriptionHighscoreMessage(game, competition, scores);
          long newHighscoreMessageId = discordService.sendMessage(discordServerId, discordChannelId, msg);
          discordService.updateHighscoreMessage(discordServerId, discordChannelId, newHighscoreMessageId);
        }
        else {
          LOG.error("Failed to resolve initial highscore of " + game.getGameDisplayName());
        }
      }
      else {
        List<Score> oldScores = scoreSummary.getScores();
        LOG.info("The current online score for " + competition + " (" + oldScores.size() + " entries):");
        for (Score oldScore : oldScores) {
          LOG.info("[" + oldScore + "]");
        }

        int position = highscoreService.calculateChangedPositionByScore(oldScores, newScore);
        if (position == -1) {
          LOG.info("No highscore change detected for subscription " + competition.getName() + ", skipping highscore message.");
        }
        else {
          List<Score> updatedScores = new ArrayList<>(oldScores);
          Score oldScore = oldScores.get(position - 1);
          updatedScores.add(position - 1, newScore);
          updatedScores = updatedScores.subList(0, updatedScores.size() - 1);

          LOG.info("Updated score post:");
          for (int i = 0; i < updatedScores.size(); i++) {
            Score s = updatedScores.get(i);
            s.setPosition(i + 1);
            LOG.info("[" + s + "]");
          }

          //update the player info for the server the message is emitted to
          Player player = this.discordService.getPlayerByInitials(discordServerId, newScore.getPlayerInitials());
          newScore.setPlayer(player);

          LOG.info("Emitting Discord highscore changed message for subscription " + competition);
          String msg = discordSubscriptionMessageFactory.createSubscriptionHighscoreCreatedMessage(game, competition, oldScore, newScore, updatedScores);
          long newHighscoreMessageId = discordService.sendMessage(discordServerId, discordChannelId, msg);
          discordService.updateHighscoreMessage(discordServerId, discordChannelId, newHighscoreMessageId);
        }
      }
    }
    LOG.info("***************** / Finished Discord Subscription Processing *********************");
  }


  @Override
  public void afterPropertiesSet() throws Exception {
    highscoreService.addHighscoreChangeListener(this);
  }
}
