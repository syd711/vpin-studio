package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.connectors.discord.DiscordMember;
import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionChangeListener;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.ScoreSummary;
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

  @Override
  public void competitionStarted(@NonNull Competition competition) {
    Game game = gameService.getGame(competition.getGameId());
    if (game != null) {
      if (competition.getType().equals(CompetitionType.DISCORD.name())) {
        highscoreService.resetHighscore(game);
        LOG.info("Resetted highscores of " + game.getGameDisplayName() + " for " + competition);

        boolean isOwner = competition.getOwner().equals(String.valueOf(discordService.getBotId()));
        long discordServerId = competition.getDiscordServerId();
        long discordChannelId = competition.getDiscordChannelId();
        long botId = discordService.getBotId();

        //check if the competition is already set as topic, in this case the user simply re-created the DB entry
        DiscordCompetitionData competitionData = discordService.getCompetitionData(discordServerId, discordChannelId);
        if ((competitionData == null && isOwner) || (competitionData != null && competitionData.isFinished() && isOwner)) {
          long messageId = discordService.sendMessage(discordServerId, discordChannelId, DiscordChannelMessageFactory.createDiscordCompetitionCreatedMessage(competition, game, botId));
          ScoreSummary highscores = highscoreService.getScoreSummary(discordServerId, game.getId(), game.getGameDisplayName());
          discordService.saveCompetitionData(competition, game, highscores, messageId);
        }
        else {
          LOG.warn("Tried to overwrite an existing competition, skipped notifications and Discord server update.");
        }
      }

      if (competition.getType().equals(CompetitionType.OFFLINE.name()) && competition.getDiscordChannelId() > 0 && competition.isActive()) {
        long discordServerId = competition.getDiscordServerId();
        long discordChannelId = competition.getDiscordChannelId();
        discordService.sendMessage(discordServerId, discordChannelId, DiscordOfflineChannelMessageFactory.createOfflineCompetitionCreatedMessage(competition, game));
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
        long discordServerId = competition.getDiscordServerId();
        long discordChannelId = competition.getDiscordChannelId();
        discordService.sendMessage(discordServerId, discordChannelId, DiscordChannelMessageFactory.createCompetitionJoinedMessage(competition, bot));

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
          String message = DiscordOfflineChannelMessageFactory.createCompetitionFinishedMessage(competition, winner, game, scoreSummary);
          discordService.sendMessage(discordServerId, discordChannelId, message);
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
        if (competition.getOwner().equals(String.valueOf(discordService.getBotId())) && competition.isActive()) {
          Player player = discordService.getPlayer(discordServerId, Long.parseLong(competition.getOwner()));
          String message = DiscordChannelMessageFactory.createCompetitionCancelledMessage(player, competition);
          discordService.sendMessage(discordServerId, discordChannelId, message);
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
