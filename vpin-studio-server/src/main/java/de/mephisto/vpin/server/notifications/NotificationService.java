package de.mephisto.vpin.server.notifications;

import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.PreferenceNames;
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
import de.mephisto.vpin.server.highscores.*;
import de.mephisto.vpin.server.highscores.cards.CardService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.popper.TableStatusChangeListener;
import de.mephisto.vpin.server.popper.TableStatusChangedEvent;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService implements InitializingBean, HighscoreChangeListener, CompetitionChangeListener, TableStatusChangeListener, DiscordHighscoreChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(NotificationService.class);

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private CardService cardService;

  @Autowired
  private GameService gameService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private PopperService popperService;

  @Autowired
  private HighscoreParser highscoreParser;

  @Autowired
  private DiscordService discordService;

  public void notifyPopperRestart() {
    discordService.setStatus(null);
  }


  @Override
  public void tableLaunched(TableStatusChangedEvent event) {
    Game game = event.getGame();
    discordService.setStatus(game.getGameDisplayName());
    highscoreService.updateHighscore(game);
  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {
    Game game = event.getGame();
    LOG.info("Executing table exit commands for '" + game + "'");
    discordService.setStatus(null);
    new Thread(() -> {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        //ignore
      }
      LOG.info("Finished 5 second update delay, updating highscores.");
      highscoreService.updateHighscore(game);
    }).start();
  }

  @Override
  public void highscoreInitialized(@NotNull HighscoreInitializedEvent event) {
    try {
      cardService.generateCard(event.getGame(), false);
    } catch (Exception e) {
      LOG.error("Error updating card after highscore initialized event: " + e.getMessage(), e);
    }
  }

  @Override
  public void highscoreChanged(@NotNull DiscordHighscoreChangeEvent event) {
    //up til now we only THAT the score has changed, but not how
    Competition competition = event.getCompetition();
    Game game = event.getGame();
    long discordServerId = competition.getDiscordServerId();
    long discordChannelId = competition.getDiscordChannelId();

    HighscoreMetadata highscoreMetaData = event.getHighscoreMetaData();
    String newRaw = highscoreMetaData.getRaw();

    LOG.info("****** Processing Discord Highscore Change Event for " + game.getGameDisplayName() + " *********");

    List<Score> newScores = highscoreService.parseScores(new Date(), newRaw, game.getId(), discordServerId);
    ScoreList scoreList = discordService.getScoreList(highscoreParser, competition.getUuid(), discordServerId, discordChannelId);
    List<Score> oldScores = null;
    ScoreSummary latestScore = scoreList.getLatestScore();
    if (latestScore == null) {
      DiscordCompetitionData competitionData = discordService.getCompetitionData(discordServerId, discordChannelId);

      oldScores = competitionData.getScores().stream().map(score -> {
        Player player = discordService.getPlayerByInitials(discordServerId, score.getInitials());
        double numericScore = HighscoreParser.toNumericScore(score.getScore());
        return new Score(highscoreMetaData.getModified(), game.getId(), score.getInitials(), player, score.getScore(), numericScore, score.getPosition());
      }).collect(Collectors.toList());
    }
    else {
      oldScores = latestScore.getScores();
    }

    int position = highscoreService.calculateChangedPosition(oldScores, newScores);
    if (position == -1) {
      LOG.info("No highscore change detected for " + game + " of discord competition '" + competition.getName() + "', skipping highscore message.");
      return;
    }

    Score oldScore = oldScores.get(position - 1);
    Score newScore = newScores.get(position - 1);

    List<Score> updatedScores = new ArrayList<>();
    for(int i=0; i< newScores.size(); i++) {
      if(i == (position - 1)) {
        updatedScores.add(newScore);
      }
      else {
        updatedScores.add(newScores.get(i));
      }
    }

    discordService.sendMessage(discordServerId, discordChannelId, DiscordChannelMessageFactory.createCompetitionHighscoreCreatedMessage(game, competition, oldScore, newScore, updatedScores));
    LOG.info("***************** / Finished Discord Highscore Processing *********************");
  }

  @Override
  public void highscoreChanged(@NotNull HighscoreChangeEvent event) {
    Game game = event.getGame();
    try {
      cardService.generateCard(game, false);
    } catch (Exception e) {
      LOG.error("Error updating card after highscore change event: " + e.getMessage(), e);
    }

    List<Competition> competitionForGame = competitionService.getCompetitionForGame(game.getId());
    boolean messageSent = false;
    for (Competition competition : competitionForGame) {
      if (competition.getDiscordChannelId() > 0 && competition.isActive()) {
        long discordServerId = competition.getDiscordServerId();
        long discordChannelId = competition.getDiscordChannelId();
        discordService.sendMessage(discordServerId, discordChannelId, DiscordOfflineChannelMessageFactory.createCompetitionHighscoreCreatedMessage(competition, event));
        messageSent = true;
      }
    }

    if (!messageSent) {
      discordService.sendDefaultHighscoreMessage(DiscordOfflineChannelMessageFactory.createHighscoreCreatedMessage(event));
    }
  }

  @Override
  public void competitionCreated(@NonNull Competition competition) {
    Game game = gameService.getGame(competition.getGameId());
    if (game != null) {

      if (competition.getType().equals(CompetitionType.DISCORD.name())) {
        long discordServerId = competition.getDiscordServerId();
        long discordChannelId = competition.getDiscordChannelId();
        long botId = discordService.getBotId();

        //check if the competition is already set as topic, in this case the user simply re-created the DB entry
        DiscordCompetitionData competitionData = discordService.getCompetitionData(discordServerId, discordChannelId);
        if (competitionData == null) {
          String messageId = discordService.sendMessage(discordServerId, discordChannelId, DiscordChannelMessageFactory.createDiscordCompetitionCreatedMessage(competition, game, botId));
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

      if (competition.getBadge() != null) {
        popperService.augmentWheel(game, competition.getBadge());
      }
    }
  }

  @Override
  public void competitionFinished(@NonNull Competition competition, @Nullable Player winner) {
    Game game = gameService.getGame(competition.getGameId());
    if (game != null) {
      LOG.info("Finishing " + competition);
      popperService.deAugmentWheel(game);

      if (competition.getDiscordChannelId() > 0) {
        long discordServerId = competition.getDiscordServerId();
        long discordChannelId = competition.getDiscordChannelId();
        ScoreSummary summary = competitionService.getCompetitionScore(competition.getId());
        if (summary != null) {
          discordService.sendMessage(discordServerId, discordChannelId, DiscordOfflineChannelMessageFactory.createCompetitionFinishedMessage(competition, winner, game, summary));
        }
        else {
          LOG.warn("Failed to finished " + competition + " properly, unable to resolve scoring from topic.");
        }
      }

      if (competition.getType().equals(CompetitionType.DISCORD.name())) {
        long discordServerId = competition.getDiscordServerId();
        long discordChannelId = competition.getDiscordChannelId();
        discordService.resetCompetition(discordServerId, discordChannelId);
      }
    }
  }

  @Override
  public void competitionDeleted(@NonNull Competition competition) {
    Game game = gameService.getGame(competition.getGameId());
    if (game != null) {
      popperService.deAugmentWheel(game);

      if (competition.getDiscordChannelId() > 0 && competition.isActive()) {
        long discordServerId = competition.getDiscordServerId();
        long discordChannelId = competition.getDiscordChannelId();
        String message = DiscordOfflineChannelMessageFactory.createCompetitionCancelledMessage(competition);
        discordService.sendMessage(discordServerId, discordChannelId, message);
      }

      if (competition.getType().equals(CompetitionType.DISCORD.name())) {
        long discordServerId = competition.getDiscordServerId();
        long discordChannelId = competition.getDiscordChannelId();
        discordService.resetCompetition(discordServerId, discordChannelId);
      }
    }
  }

  @Override
  public void competitionChanged(@NonNull Competition competition) {
    Game game = gameService.getGame(competition.getGameId());
    if (game != null) {
      if (competition.getBadge() != null) {
        popperService.augmentWheel(game, competition.getBadge());
      }
      else {
        popperService.deAugmentWheel(game);
      }
    }

    runAugmentationCheck();

    //only the dates of the competition could have been changed
    if (competition.getType().equals(CompetitionType.DISCORD.name())) {
      ScoreSummary summary = competitionService.getCompetitionScore(competition.getId());
      long discordServerId = competition.getDiscordServerId();
      long discordChannelId = competition.getDiscordChannelId();

      String messageId = discordService.getStartMessageId(discordServerId, discordChannelId);
      discordService.saveCompetitionData(competition, game, summary, messageId);
    }
  }

  private void runAugmentationCheck() {
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
    highscoreService.addHighscoreChangeListener(this);
    highscoreService.addDiscordHighscoreChangeListener(this);
    competitionService.addCompetitionChangeListener(this);
    popperService.addTableStatusChangeListener(this);
    discordService.setStatus(null);
  }
}
