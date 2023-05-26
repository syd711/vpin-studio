package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.connectors.discord.DiscordMember;
import de.mephisto.vpin.restclient.CompetitionType;
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
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.popper.PopperService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompetitionChangeListenerImpl implements InitializingBean, CompetitionChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionChangeListenerImpl.class);

  @Autowired
  private CompetitionService competitionService;

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

  @Override
  public void competitionStarted(@NonNull Competition competition) {
    Game game = gameService.getGame(competition.getGameId());
    if (game != null) {
      if (competition.getType().equals(CompetitionType.DISCORD.name())) {
        boolean isOwner = competition.getOwner().equals(String.valueOf(discordService.getBotId()));
        long serverId = competition.getDiscordServerId();
        long channelId = competition.getDiscordChannelId();
        long botId = discordService.getBotId();

        if (isOwner) {
          String base64Data = CompetitionDataHelper.DATA_INDICATOR + CompetitionDataHelper.toBase64(competition, game);
          byte[] image = assetService.getCompetitionBackgroundFor(competition, game);
          String message = DiscordChannelMessageFactory.createDiscordCompetitionCreatedMessage(botId, competition.getUuid());

          long messageId = discordService.sendMessage(serverId, channelId, message, image, competition.getName() + ".png", base64Data);
          //since we started a new competition, all messages before today are irrelevant (we check only today so we don't run into topic update limits)
          discordService.initCompetition(serverId, channelId, messageId);
          LOG.info("Finished Discord update of \"" + competition.getName() + "\"");
        }
        else {
          if (!discordService.isCompetitionActive(serverId, channelId, competition.getUuid())) {
            LOG.warn("The start of competition \"" + competition.getName() + "\" has been cancelled, because its no longer valid. " +
                "The competition will be close during the next check.");
            return;
          }
        }

        highscoreService.resetHighscore(game);
        LOG.info("Resetted highscores of " + game.getGameDisplayName() + " for " + competition);
      }

      if (competition.getType().equals(CompetitionType.OFFLINE.name()) && competition.getDiscordChannelId() > 0 && competition.isActive()) {
        long discordServerId = competition.getDiscordServerId();
        long discordChannelId = competition.getDiscordChannelId();

        byte[] image = assetService.getCompetitionBackgroundFor(competition, game);
        String message = DiscordOfflineChannelMessageFactory.createOfflineCompetitionCreatedMessage(competition, game);
        discordService.sendMessage(discordServerId, discordChannelId, message, image, competition.getName() + ".png", "This is an offline competition. Other player bots can't join.");
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
        long msgId = discordService.sendMessage(discordServerId, discordChannelId, DiscordChannelMessageFactory.createCompetitionJoinedMessage(competition, bot));
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

      long discordServerId = competition.getDiscordServerId();
      long discordChannelId = competition.getDiscordChannelId();

      if (competition.getType().equals(CompetitionType.OFFLINE.name())) {
        String message = DiscordOfflineChannelMessageFactory.createCompetitionFinishedMessage(competition, winner, game, scoreSummary);
        discordService.sendMessage(discordServerId, discordChannelId, message);
      }

      if (competition.getType().equals(CompetitionType.DISCORD.name())) {
        //only the owner can perform additional actions
        if (competition.getOwner().equals(String.valueOf(discordService.getBotId()))) {
          String message = DiscordChannelMessageFactory.createCompetitionFinishedMessage(competition, winner, game, scoreSummary);
          discordService.sendMessage(discordServerId, discordChannelId, message);
          LOG.info("Clearing pinned messages for " + competition.getName());
          discordService.clearPinnedMessages(discordServerId, discordChannelId);
        }
      }
    }
  }

  @Override
  public void competitionDeleted(@NonNull Competition competition) {
    Game game = gameService.getGame(competition.getGameId());
    if (game != null) {
      runCheckedDeAugmentation();

      long discordServerId = competition.getDiscordServerId();
      long discordChannelId = competition.getDiscordChannelId();

      if (competition.getType().equals(CompetitionType.OFFLINE.name()) && discordChannelId > 0 && competition.isActive()) {
        String message = DiscordOfflineChannelMessageFactory.createCompetitionCancelledMessage(competition);
        discordService.sendMessage(discordServerId, discordChannelId, message);
      }

      if (competition.getType().equals(CompetitionType.DISCORD.name())) {
        //check if the owner deleted the competition
        boolean isOwner = competition.getOwner().equals(String.valueOf(discordService.getBotId()));
        if (isOwner && competition.isActive()) {
          Player player = discordService.getPlayer(discordServerId, Long.parseLong(competition.getOwner()));
          String message = DiscordChannelMessageFactory.createCompetitionCancelledMessage(player, competition);
          discordService.sendMessage(discordServerId, discordChannelId, message);
        }

        //remove from active player list
        if(!isOwner && competition.isActive()) {
          discordService.removeCompetitionPlayer(discordServerId, discordChannelId);
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
