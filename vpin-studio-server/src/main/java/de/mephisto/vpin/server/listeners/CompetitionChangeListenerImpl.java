package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.connectors.discord.DiscordMember;
import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.assets.AssetService;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionChangeListener;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.discord.CompetitionDataHelper;
import de.mephisto.vpin.server.discord.DiscordChannelMessageFactory;
import de.mephisto.vpin.server.discord.DiscordOfflineChannelMessageFactory;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompetitionChangeListenerImpl implements InitializingBean, CompetitionChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionChangeListenerImpl.class);

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private DiscordService discordService;

  @Autowired
  private GameService gameService;

  @Autowired
  private PopperService popperService;

  @Autowired
  private AssetService assetService;

  @Autowired
  private DiscordChannelMessageFactory discordChannelMessageFactory;

  @Override
  public void competitionStarted(@NonNull Competition competition) {
    Game game = gameService.getGame(competition.getGameId());
    if (game != null) {
      if (competition.getType().equals(CompetitionType.DISCORD.name())) {
        boolean isOwner = competition.getOwner().equals(String.valueOf(discordService.getBotId()));
        long serverId = competition.getDiscordServerId();
        long channelId = competition.getDiscordChannelId();
        long botId = discordService.getBotId();

        //check if the database has been resetted and we only join a competition that we created earlier
        boolean active = discordService.isCompetitionActive(serverId, channelId, competition.getUuid());
        if (active) {
          LOG.info("The " + competition + " is still available and active, skipping init process.");
        }
        else if (isOwner) {
          String description = "This is an online competition and player bots can join it.\nUse the **initials of your bot** when you create a new highscore.\n" +
              "Only these will be submitted to the competition.\nCompetition updates are pinned on this channel.\n\n";
          String base64Data = CompetitionDataHelper.DATA_INDICATOR + CompetitionDataHelper.toBase64(competition, game);
          byte[] image = assetService.getCompetitionStartedCard(competition, game);
          String message = discordChannelMessageFactory.createDiscordCompetitionCreatedMessage(competition.getDiscordServerId(), botId, competition.getUuid());

          long messageId = discordService.sendMessage(serverId, channelId, message, image, competition.getName() + ".png", description + base64Data);
          discordService.initCompetition(serverId, channelId, messageId);
          LOG.info("Finished Discord update of \"" + competition.getName() + "\"");
        }

        highscoreService.resetHighscore(game);
        LOG.info("Resetted highscores of " + game.getGameDisplayName() + " for " + competition);
      }

      if (competition.getType().equals(CompetitionType.OFFLINE.name())) {
        if (competition.getDiscordChannelId() > 0 && competition.isActive()) {
          long serverId = competition.getDiscordServerId();
          long channelId = competition.getDiscordChannelId();

          DiscordMember bot = discordService.getBot();
          if (serverId > 0 && channelId > 0 && bot != null) {
            byte[] image = assetService.getCompetitionStartedCard(competition, game);
            String message = DiscordOfflineChannelMessageFactory.createOfflineCompetitionCreatedMessage(competition, game);
            String vPinName = (String) preferencesService.getPreferenceValue(PreferenceNames.SYSTEM_NAME, "My VPin");
            String subText = "This is an offline competition. Only players on \"" + vPinName + "\" can participate.";
            if (competition.isHighscoreReset()) {
              subText += "\n\nThe highscore of this table has been resetted.";
            }
            else {
              subText += "\nThe highscore of this table has not been resetted.";
              Optional<Highscore> hs = highscoreService.getOrCreateHighscore(game);
              if (hs.isPresent() && !StringUtils.isEmpty(hs.get().getRaw())) {
                subText += "\nHere is the current highscore:\n\n```" + hs.get().getRaw() + "```";
              }
            }
            discordService.sendMessage(serverId, channelId, message, image, competition.getName() + ".png", subText);
          }

          if (competition.isHighscoreReset()) {
            highscoreService.resetHighscore(game);
          }
        }
      }

      if (competition.getBadge() != null && competition.isActive()) {
        popperService.augmentWheel(game, competition.getBadge());
      }
    }
  }

  @Override
  public void competitionCreated(@NonNull Competition competition) {
    if (competition.getType().equals(CompetitionType.DISCORD.name())) {
      Game game = gameService.getGame(competition.getGameId());
      boolean isOwner = competition.getOwner().equals(String.valueOf(discordService.getBotId()));
      DiscordMember bot = discordService.getBot();
      if (game != null && !isOwner && bot != null) {
        highscoreService.resetHighscore(game);

        long discordServerId = competition.getDiscordServerId();
        long discordChannelId = competition.getDiscordChannelId();
        long msgId = discordService.sendMessage(discordServerId, discordChannelId, discordChannelMessageFactory.createCompetitionJoinedMessage(competition, bot));
        discordService.addCompetitionPlayer(discordServerId, discordChannelId, msgId);

        LOG.info("Discord bot \"" + bot + "\" has joined \"" + competition + "\"");
      }
    }
  }

  @Override
  public void competitionFinished(@NonNull Competition competition, @Nullable Player winner, @NonNull ScoreSummary scoreSummary) {
    Game game = gameService.getGame(competition.getGameId());
    if (game != null) {
      runCheckedDeAugmentation();

      long serverId = competition.getDiscordServerId();
      long channelId = competition.getDiscordChannelId();

      if (competition.getType().equals(CompetitionType.OFFLINE.name())) {
        DiscordMember bot = discordService.getBot();
        if (bot != null && serverId > 0 && channelId > 0) {
          boolean noWinner = scoreSummary.getScores().isEmpty();
          String message = DiscordOfflineChannelMessageFactory.createCompetitionFinishedMessage(competition, winner, game, scoreSummary);
          //check if a winner card should be generated
          if (noWinner) {
            discordService.sendMessage(serverId, channelId, message);
          }
          else {
            Platform.runLater(() -> {
              byte[] image = assetService.getCompetitionFinishedCard(competition, game, winner, scoreSummary);
              discordService.sendMessage(serverId, channelId, message, image, competition.getName() + ".png", null);
            });
          }
        }
        else {
          LOG.warn("Skipped finish notification for " + competition + ", invalid Discord configuration.");
        }
      }

      if (competition.getType().equals(CompetitionType.DISCORD.name())) {
        //only the owner can perform additional actions
        if (competition.getOwner().equals(String.valueOf(discordService.getBotId()))) {
          boolean noWinner = scoreSummary.getScores().isEmpty();
          String message = discordChannelMessageFactory.createCompetitionFinishedMessage(competition, scoreSummary);
          //check if a winner card should be generated
          if (noWinner) {
            long msgId = discordService.sendMessage(serverId, channelId, message);
            discordService.finishCompetition(serverId, channelId, msgId);
          }
          else {
            Platform.runLater(() -> {
              byte[] image = assetService.getCompetitionFinishedCard(competition, game, winner, scoreSummary);
              long msgId = discordService.sendMessage(serverId, channelId, message, image, competition.getName() + ".png", null);
              discordService.finishCompetition(serverId, channelId, msgId);
            });
          }
        }
        else {
          LOG.warn("Skipped finish notification, you are not the owner of " + competition);
        }
      }
    }
  }

  @Override
  public void competitionDeleted(@NonNull Competition competition) {
    Game game = gameService.getGame(competition.getGameId());
    if (game != null) {
      runCheckedDeAugmentation();

      long serverId = competition.getDiscordServerId();
      long channelId = competition.getDiscordChannelId();

      if (competition.getType().equals(CompetitionType.OFFLINE.name()) && channelId > 0 && competition.isActive()) {
        if (serverId > 0 && channelId > 0) {
          String message = DiscordOfflineChannelMessageFactory.createCompetitionCancelledMessage(competition);
          discordService.sendMessage(serverId, channelId, message);
        }
      }

      if (competition.getType().equals(CompetitionType.DISCORD.name())) {
        //check if the owner deleted the competition
        boolean isOwner = competition.getOwner().equals(String.valueOf(discordService.getBotId()));
        if (isOwner && competition.isActive()) {
          Player player = discordService.getPlayer(serverId, Long.parseLong(competition.getOwner()));
          String message = discordChannelMessageFactory.createCompetitionCancelledMessage(player, competition);
          long msgId = discordService.sendMessage(serverId, channelId, message);
          discordService.finishCompetition(serverId, channelId, msgId);
        }

        //remove from active player list
        if (!isOwner && competition.isActive()) {
          discordService.removeCompetitionPlayer(serverId, channelId);
        }
      }
    }
  }

  @Override
  public void competitionChanged(@NonNull Competition competition) {
    Game game = gameService.getGame(competition.getGameId());
    boolean active = competition.isActive();

    //the data has already been saved, check other changes, like the badge
    if (game != null && active) {
      if (competition.getBadge() != null) {
        popperService.augmentWheel(game, competition.getBadge());
      }
    }
    runCheckedDeAugmentation();
  }

  /**
   * Checks if there are any augmented wheel icons that do not belong
   * to any competition anymore.
   */
  private void runCheckedDeAugmentation() {
    List<Integer> competedGameIds = competitionService.getActiveCompetitions().stream().map(Competition::getGameId).collect(Collectors.toList());
    List<Game> games = gameService.getGames();
    for (Game game : games) {
      if (!competedGameIds.contains(game.getId())) {
        popperService.deAugmentWheel(game);
      }
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    competitionService.addCompetitionChangeListener(this);
  }
}
