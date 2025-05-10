package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.connectors.discord.DiscordMember;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.server.assets.AssetService;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionLifecycleService;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.discord.CompetitionDataHelper;
import de.mephisto.vpin.server.discord.DiscordChannelMessageFactory;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreBackupService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.vps.VpsService;
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

@Service
public class DiscordCompetitionChangeListenerImpl extends DefaultCompetitionChangeListener implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(DiscordCompetitionChangeListenerImpl.class);

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private CompetitionLifecycleService competitionLifecycleService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private DiscordService discordService;

  @Autowired
  private GameService gameService;

  @Autowired
  private FrontendStatusService frontendStatusService;

  @Autowired
  private AssetService assetService;

  @Autowired
  private HighscoreBackupService highscoreBackupService;

  @Autowired
  private DiscordChannelMessageFactory discordChannelMessageFactory;

  @Autowired
  private VpsService vpsService;


  @Override
  public void competitionStarted(@NonNull Competition competition) {
    if (competition.getType().equals(CompetitionType.DISCORD.name())) {
      try {
        Game game = gameService.getGame(competition.getGameId());
        if (game == null) {
          LOG.info("No game found for id " + competition.getGameId() + ", seems it has been removed before the competition was started.");
          return;
        }

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
          final String description = "This is an online competition and player bots can join it.\nUse the **initials of your bot** when you create a new highscore.\n" +
              "Only these will be submitted to the competition.\nCompetition updates are pinned on this channel.";
          String base64Data = CompetitionDataHelper.DATA_INDICATOR + CompetitionDataHelper.toBase64(competition, game);
          byte[] image = assetService.getCompetitionStartedCard(competition, game);
          final String message = discordChannelMessageFactory.createDiscordCompetitionCreatedMessage(competition.getDiscordServerId(), botId, competition.getUuid());

          //do not emit these messages asynchronously, because a finish check is triggered right afterwards and would finish the competition if no message is found
          String imageMessage = description;
          if (!StringUtils.isEmpty(game.getExtTableId())) {
            VpsTable vpsTable = vpsService.getTableById(game.getExtTableId());
            imageMessage += "\n\nVirtual Pinball Spreadsheet:\n" + VPS.getVpsTableUrl(game.getExtTableId());

            if (!StringUtils.isEmpty(game.getExtTableVersionId())) {
              List<VpsTableVersion> tableFiles = vpsTable.getTableFiles();
              Optional<VpsTableVersion> tableVersion = tableFiles.stream().filter(t -> t.getId().equals(game.getExtTableVersionId())).findFirst();
              if (tableVersion.isPresent() && !tableVersion.get().getUrls().isEmpty()) {
                imageMessage += "\n\nTable Download:\n" + tableVersion.get().getUrls().get(0).getUrl();
              }
            }
          }
          long messageId = discordService.sendMessage(serverId, channelId, message, image, competition.getName() + ".png", imageMessage + "\n\n" + base64Data);
          discordService.initCompetition(serverId, channelId, messageId, "Competition Channel for Table \"" + game.getGameDisplayName() + "\"");
          LOG.info("Finished Discord update of \"" + competition.getName() + "\"");
        }

        if (highscoreBackupService.backup(game)) {
          highscoreService.resetHighscore(game);
        }
        LOG.info("Resetted highscores of " + game.getGameDisplayName() + " for " + competition);

        if (competition.getBadge() != null && competition.isActive()) {
          frontendStatusService.augmentWheel(game, competition.getBadge());
        }
      } catch (Exception e) {
        LOG.error("Error starting discord competition: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void competitionCreated(@NonNull Competition competition) {
    if (competition.getType().equals(CompetitionType.DISCORD.name())) {
      try {
        Game game = gameService.getGame(competition.getGameId());
        boolean isOwner = competition.getOwner().equals(String.valueOf(discordService.getBotId()));
        DiscordMember bot = discordService.getBot();

        //this is the situation where a player joined a competition
        if (game != null && !isOwner && bot != null) {
          if (highscoreBackupService.backup(game)) {
            highscoreService.resetHighscore(game);
          }

          long discordServerId = competition.getDiscordServerId();
          long discordChannelId = competition.getDiscordChannelId();
          long msgId = discordService.sendMessage(discordServerId, discordChannelId, discordChannelMessageFactory.createCompetitionJoinedMessage(competition, bot));
          discordService.addCompetitionPlayer(discordServerId, discordChannelId, msgId);

          LOG.info("Discord bot \"" + bot + "\" has joined \"" + competition + "\"");
        }
      } catch (Exception e) {
        LOG.error("Error creating discord competition: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void competitionFinished(@NonNull Competition competition, @Nullable Player winner, @NonNull ScoreSummary scoreSummary) {
    if (competition.getType().equals(CompetitionType.DISCORD.name())) {
      Game game = gameService.getGame(competition.getGameId());
      if (game != null) {
        runCheckedDeAugmentation(competitionService, gameService, frontendStatusService);

        long serverId = competition.getDiscordServerId();
        long channelId = competition.getDiscordChannelId();

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
                String description = "";
                if (!scoreSummary.getScores().isEmpty()) {
                  description = "Here are the final results:\n" + DiscordChannelMessageFactory.createHighscoreList(scoreSummary.getScores(), competition.getScoreLimit());
                }
                description = description + "\nYou can duplicate the competition to continue it with another table or duration.";
                byte[] image = assetService.getCompetitionFinishedCard(competition, game, winner, scoreSummary);
                long msgId = discordService.sendMessage(serverId, channelId, message, image, competition.getName() + ".png", description);
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
  }

  @Override
  public void competitionDeleted(@NonNull Competition competition) {
    if (competition.getType().equals(CompetitionType.DISCORD.name())) {
      Game game = gameService.getGame(competition.getGameId());
      if (game != null) {
        runCheckedDeAugmentation(competitionService, gameService, frontendStatusService);

        long serverId = competition.getDiscordServerId();
        long channelId = competition.getDiscordChannelId();

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
  public void afterPropertiesSet() throws Exception {
    competitionLifecycleService.addCompetitionChangeListener(this);
  }
}
