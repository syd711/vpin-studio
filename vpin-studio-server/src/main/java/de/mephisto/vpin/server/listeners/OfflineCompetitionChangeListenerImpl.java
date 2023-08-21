package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.connectors.discord.DiscordMember;
import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.assets.AssetService;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.ScoreSummary;
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

import java.util.Optional;

@Service
public class OfflineCompetitionChangeListenerImpl extends DefaultCompetitionChangeListener implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(OfflineCompetitionChangeListenerImpl.class);

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

  @Override
  public void competitionStarted(@NonNull Competition competition) {
    if (competition.getType().equals(CompetitionType.OFFLINE.name())) {
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
  public void competitionFinished(@NonNull Competition competition, @Nullable Player winner, @NonNull ScoreSummary scoreSummary) {
    if (competition.getType().equals(CompetitionType.OFFLINE.name())) {
      Game game = gameService.getGame(competition.getGameId());
      if (game != null) {
        runCheckedDeAugmentation(competitionService, gameService, popperService);

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
      }
    }
  }

  @Override
  public void competitionDeleted(@NonNull Competition competition) {
    if (competition.getType().equals(CompetitionType.OFFLINE.name())) {
      Game game = gameService.getGame(competition.getGameId());
      if (game != null) {
        runCheckedDeAugmentation(competitionService, gameService, popperService);

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
    competitionService.addCompetitionChangeListener(this);
  }
}
