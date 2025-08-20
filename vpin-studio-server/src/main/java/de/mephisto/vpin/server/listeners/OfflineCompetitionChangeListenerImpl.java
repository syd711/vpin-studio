package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.connectors.discord.DiscordMember;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.assets.AssetService;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionLifecycleService;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.discord.DiscordChannelMessageFactory;
import de.mephisto.vpin.server.discord.DiscordOfflineChannelMessageFactory;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreBackupService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.HighscoreParsingService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
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

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OfflineCompetitionChangeListenerImpl extends DefaultCompetitionChangeListener implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(OfflineCompetitionChangeListenerImpl.class);

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private CompetitionLifecycleService competitionLifecycleService;

  @Autowired
  private PreferencesService preferencesService;

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
  private HighscoreParsingService highscoreParsingService;

  @Override
  public void competitionStarted(@NonNull Competition competition) {
    if (competition.getType().equals(CompetitionType.OFFLINE.name())) {
      try {
        Game game = gameService.getGame(competition.getGameId());
        if (game != null) {
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
                subText += "\nThe highscore of this table has been resetted.";
              }
              else {
                subText += "\nThe highscore of this table has not been resetted.";
                Optional<Highscore> hs = highscoreService.getHighscore(game, true, EventOrigin.USER_INITIATED);
                if (hs.isPresent() && !StringUtils.isEmpty(hs.get().getRaw())) {
                  String raw = hs.get().getRaw();
                  List<Score> scores = highscoreParsingService.parseScores(new Date(), raw, game, -1);
                  String highscoreList = DiscordChannelMessageFactory.createHighscoreList(scores, -1);
                  subText += "\nHere is the current highscore:\n\n" + highscoreList;
                }
              }
              discordService.sendMessage(serverId, channelId, message, image, competition.getName() + ".png", subText);
            }
          }

          if (competition.isHighscoreReset()) {
            if (highscoreBackupService.backup(game) != null) {
              highscoreService.resetHighscore(game);
            }
          }
        }

        if (competition.getBadge() != null && competition.isActive()) {
          frontendStatusService.augmentWheel(game, competition.getBadge());
        }
      } catch (Exception e) {
        LOG.error("Error creating offline competition: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void competitionFinished(@NonNull Competition competition, @Nullable Player winner, @NonNull ScoreSummary scoreSummary) {
    if (competition.getType().equals(CompetitionType.OFFLINE.name())) {
      Game game = gameService.getGame(competition.getGameId());
      if (game != null) {
        runCheckedDeAugmentation(competitionService, gameService, frontendStatusService);

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
                List<Score> scores = highscoreParsingService.parseScores(new Date(), scoreSummary.getRaw(), game, -1);
                String highscoreList = DiscordChannelMessageFactory.createHighscoreList(scores, -1);
                String imageMessage = "Here are the final results:\n" + highscoreList + "\nYou can duplicate the competition to continue it with another table or duration.";
                discordService.sendMessage(serverId, channelId, message, image, competition.getName() + ".png", imageMessage);
              });
            }
          }
          else {
            LOG.warn("Skipped finish notification for " + competition + ", invalid Discord configuration.");
          }
        }
      }
    }
  }

  @Override
  public void competitionDeleted(@NonNull Competition competition) {
    if (competition.getType().equals(CompetitionType.OFFLINE.name())) {
      Game game = gameService.getGame(competition.getGameId());
      if (game != null) {
        runCheckedDeAugmentation(competitionService, gameService, frontendStatusService);

        long serverId = competition.getDiscordServerId();
        long channelId = competition.getDiscordChannelId();

        if (channelId > 0 && serverId > 0 && competition.isActive()) {
          String message = DiscordOfflineChannelMessageFactory.createCompetitionCancelledMessage(competition);
          discordService.sendMessage(serverId, channelId, message);
        }
      }
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    competitionLifecycleService.addCompetitionChangeListener(this);
  }
}
