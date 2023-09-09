package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.SubscriptionInfo;
import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionScoreValidator;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreParser;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.Player;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DiscordCompetitionService {
  private final static Logger LOG = LoggerFactory.getLogger(DiscordCompetitionService.class);

  @Autowired
  private DiscordService discordService;

  @Autowired
  private HighscoreParser highscoreParser;

  @Autowired
  private DiscordChannelMessageFactory discordChannelMessageFactory;

  @Autowired
  private DiscordSubscriptionMessageFactory discordSubscriptionMessageFactory;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private GameService gameService;

  public List<Score> runCompetitionCheck(long id) {
    Competition competition = competitionService.getCompetition(id);
    if (competition != null) {
      if (competition.getType().equals(CompetitionType.DISCORD.name())) {
        Game game = gameService.getGame(competition.getGameId());
        highscoreService.deleteScores(game.getId(), false);
        highscoreService.scanScore(game);
      }
    }
    return Collections.emptyList();
  }

  /**
   * Up til now we only THAT the score has changed, but not how.
   *
   * @param game            the game competed
   * @param newScore        the new highscore that has been submitted
   * @param competition     the online competition the score is for
   * @param competitionData the data stored on the channel
   */
  public void runDiscordServerUpdate(@NonNull Game game, @NonNull Score newScore, @NonNull Competition competition, @Nullable DiscordCompetitionData competitionData) {
    DiscordBotStatus botStatus = discordService.getStatus(competition.getDiscordServerId());

    LOG.info("****** Processing Discord Highscore Change Event for " + game.getGameDisplayName() + " | " + competition.getType() + " *********");
    LOG.info("The new score: " + newScore);
    String validationMsg = CompetitionScoreValidator.validate(competitionData, game, competition, newScore, botStatus);
    if (validationMsg != null) {
      LOG.error("Highscore Validation Error: " + validationMsg);
      return;
    }

    long discordServerId = competition.getDiscordServerId();
    long discordChannelId = competition.getDiscordChannelId();
    ScoreSummary discordScoreSummary = discordService.getScoreSummary(highscoreParser, competition.getUuid(), discordServerId, discordChannelId);
    if (discordScoreSummary.getScores().isEmpty()) {
      LOG.info("Emitting initial highscore message for " + competition);
      int scoreLimit = resolveScoreLimit(competition, competitionData);
      String msg = createInitialHighscoreMessage(game, newScore, competition, scoreLimit);
      long newHighscoreMessageId = discordService.sendMessage(discordServerId, discordChannelId, msg);
      discordService.updateHighscoreMessage(discordServerId, discordChannelId, newHighscoreMessageId);
    }
    else {
      List<Score> oldScores = discordScoreSummary.getScores();
      LOG.info("The current online score for " + competition + " (" + oldScores.size() + " entries):");
      for (Score oldScore : oldScores) {
        LOG.info("[" + oldScore + "]");
      }

      if (discordScoreSummary.contains(newScore)) {
        LOG.info("The score " + newScore + " already exists in channels highscore list, skipping update");
        return;
      }

      int position = highscoreService.calculateChangedPositionByScore(oldScores, newScore);
      if (position == -1) {
        LOG.info("No highscore change detected for " + game + " of discord competition '" + competition.getName() + "', skipping highscore message.");
      }
      else {
        List<Score> updatedScores = new ArrayList<>(oldScores);
        Score oldScore = oldScores.get(position - 1);
        updatedScores.add(position - 1, newScore);

        updatedScores = sanitizeScoreList(competition, updatedScores, competitionData);

        //update the player info for the server the message is emitted to
        Player player = this.discordService.getPlayerByInitials(discordServerId, newScore.getPlayerInitials());
        newScore.setPlayer(player);

        LOG.info("Emitting Discord highscore changed message for discord competition " + competition);
        String msg = createHighscoreMessage(game, newScore, competition, updatedScores, oldScore);
        long newHighscoreMessageId = discordService.sendMessage(discordServerId, discordChannelId, msg);
        discordService.updateHighscoreMessage(discordServerId, discordChannelId, newHighscoreMessageId);
      }
    }
    LOG.info("***************** / Finished Discord Highscore Processing *********************");
  }

  private String createHighscoreMessage(@NonNull Game game, @NonNull Score newScore, @NonNull Competition competition, List<Score> updatedScores, Score oldScore) {
    if (competition.getType().equalsIgnoreCase(CompetitionType.SUBSCRIPTION.name())) {
      return discordSubscriptionMessageFactory.createSubscriptionHighscoreCreatedMessage(game, competition, oldScore, newScore, updatedScores);
    }
    return discordChannelMessageFactory.createCompetitionHighscoreCreatedMessage(game, competition, oldScore, newScore, updatedScores);
  }

  private String createInitialHighscoreMessage(@NonNull Game game, @NonNull Score newScore, @NonNull Competition competition, int scoreLimit) {
    if (competition.getType().equalsIgnoreCase(CompetitionType.SUBSCRIPTION.name())) {
      return discordSubscriptionMessageFactory.createFirstSubscriptionHighscoreMessage(game, competition,  newScore, scoreLimit);
    }
    return discordChannelMessageFactory.createFirstCompetitionHighscoreCreatedMessage(game, competition, newScore, scoreLimit);
  }


  private List<Score> sanitizeScoreList(@NonNull Competition competition, @NonNull List<Score> updatedScores, @Nullable DiscordCompetitionData competitionData) {
    int index = 1;

    //filter duplicates
    List<Score> sanitized = new ArrayList<>();
    for (Score updatedScore : updatedScores) {
      Optional<Score> first = sanitized.stream().filter(s -> !s.getPlayerInitials().contains("?") && s.matches(updatedScore)).findFirst();
      if (first.isPresent()) {
        LOG.warn("Found duplicated score " + updatedScore);
        continue;
      }
      updatedScore.setPosition(index);
      sanitized.add(updatedScore);
      index++;
    }

    //append results until the score limit is met
    if (competitionData != null && competitionData.getScrL() > 0) {
      int scoreCount = competitionData.getScrL();
      while (sanitized.size() <= scoreCount) {
        Score score = new Score(new Date(), competition.getGameId(), "???", null, "0", 0, index);
        sanitized.add(score);
        index++;
        LOG.info("Appended empty default score: " + score);
      }
    }

    //check if the list was too long from the start
    int scoreLimit = resolveScoreLimit(competition, competitionData);

    if (sanitized.size() > scoreLimit) {
      sanitized = sanitized.subList(0, scoreLimit);
    }

    //duplicate, but whatever
    for (int i = 0; i < sanitized.size(); i++) {
      Score s = sanitized.get(i);
      s.setPosition(i + 1);
      LOG.info("[" + s + "]");
    }

    return sanitized;
  }

  private static int resolveScoreLimit(@NonNull Competition competition, @Nullable DiscordCompetitionData competitionData) {
    int scoreLimit = 5;
    if(competitionData != null && competitionData.getScrL() > 0) {
      scoreLimit = competitionData.getScrL();
    }
    else if(competition.getType().equals(CompetitionType.SUBSCRIPTION.name())) {
      scoreLimit = SubscriptionInfo.DEFAULT_SCORE_LIMIT;
    }
    return scoreLimit;
  }

}
