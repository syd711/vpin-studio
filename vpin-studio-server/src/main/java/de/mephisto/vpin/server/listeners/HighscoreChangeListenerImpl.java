package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.discord.DiscordChannelMessageFactory;
import de.mephisto.vpin.server.discord.DiscordOfflineChannelMessageFactory;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.*;
import de.mephisto.vpin.server.highscores.cards.CardService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HighscoreChangeListenerImpl implements InitializingBean, HighscoreChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreChangeListenerImpl.class);

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private CardService cardService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private DiscordService discordService;

  @Autowired
  private HighscoreParser highscoreParser;

  @Override
  public void highscoreInitialized(@NotNull HighscoreInitializedEvent event) {
    try {
      cardService.generateCard(event.getGame(), false);
    } catch (Exception e) {
      LOG.error("Error updating card after highscore initialized event: " + e.getMessage(), e);
    }
  }

  @Override
  public void highscoreChanged(@NotNull HighscoreChangeEvent event) {
    Game game = event.getGame();
    try {
      cardService.generateCard(game, false);
    } catch (Exception e) {
      LOG.error("Error updating card after highscore change event: " + e.getMessage(), e);
    }

    //find competition to notify about highscore updates
    List<Competition> competitionForGame = competitionService.getCompetitionForGame(game.getId());
    boolean messageSent = false;
    for (Competition competition : competitionForGame) {
      if (competition.getDiscordChannelId() > 0 && competition.isActive()) {
        long discordServerId = competition.getDiscordServerId();
        long discordChannelId = competition.getDiscordChannelId();

        if (competition.getType().equals(CompetitionType.OFFLINE.name())) {
          discordService.sendMessage(discordServerId, discordChannelId, DiscordOfflineChannelMessageFactory.createCompetitionHighscoreCreatedMessage(competition, event));
        }
        else if (competition.getType().equals(CompetitionType.DISCORD.name())) {
          discordCompetitionHighscoreChanged(event, competition);
        }
        messageSent = true;
      }
    }

    //send the default message if no competition updates was sent
    if (!messageSent) {
      LOG.info("No competition found for " + game + ", sending default notification.");
      discordService.sendDefaultHighscoreMessage(DiscordOfflineChannelMessageFactory.createHighscoreCreatedMessage(event));
    }
  }

  private void discordCompetitionHighscoreChanged(@NotNull HighscoreChangeEvent event, @NonNull Competition competition) {
    //up til now we only THAT the score has changed, but not how
    Game game = event.getGame();
    Score newScore = event.getNewScore();

    long discordServerId = competition.getDiscordServerId();
    long discordChannelId = competition.getDiscordChannelId();

    LOG.info("****** Processing Discord Highscore Change Event for " + game.getGameDisplayName() + " *********");
    LOG.info("The new score: " + newScore);

    ScoreList scoreList = discordService.getScoreList(highscoreParser, competition.getUuid(), discordServerId, discordChannelId);
    ScoreSummary latestScore = scoreList.getLatestScore();
    List<Score> oldScores = latestScore.getScores();
    LOG.info("Current discord scores:");
    for (Score oldScore : oldScores) {
      LOG.info("[" + oldScore + "]");
    }

    int position = highscoreService.calculateChangedPositionByScore(oldScores, event.getNewScore());
    if (position == -1) {
      LOG.info("No highscore change detected for " + game + " of discord competition '" + competition.getName() + "', skipping highscore message.");
    }
    else {
      Score oldScore = oldScores.get(position - 1);
      List<Score> updatedScores = new ArrayList<>();
      for (int i = 0; i < oldScores.size(); i++) {
        if ((i + 1) == position) {
          updatedScores.add(newScore);
        }
        if (updatedScores.size() <= oldScores.size()) {
          updatedScores.add(oldScores.get(i));
        }
      }

      LOG.info("Updated score post:");
      for (int i = 0; i < updatedScores.size(); i++) {
        Score s = updatedScores.get(i);
        s.setPosition(i + 1);
        LOG.info("[" + s + "]");
      }

      LOG.info("Emitting Discord highscore changed message for discord competition '" + competition + "'");
      discordService.sendMessage(discordServerId, discordChannelId, DiscordChannelMessageFactory.createCompetitionHighscoreCreatedMessage(game, competition, oldScore, newScore, updatedScores));
    }
    LOG.info("***************** / Finished Discord Highscore Processing *********************");
  }


  @Override
  public void afterPropertiesSet() throws Exception {
    highscoreService.addHighscoreChangeListener(this);
  }
}
