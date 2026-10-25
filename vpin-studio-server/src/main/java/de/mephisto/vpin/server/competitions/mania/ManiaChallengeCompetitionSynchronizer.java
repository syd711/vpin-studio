package de.mephisto.vpin.server.competitions.mania;

import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.connectors.mania.model.DiscordChallenge;
import de.mephisto.vpin.connectors.mania.model.DiscordChallengeOptOut;
import de.mephisto.vpin.connectors.mania.model.DiscordChallengesResponse;
import de.mephisto.vpin.connectors.mania.model.User;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionIdUpdater;
import de.mephisto.vpin.server.competitions.CompetitionLifecycleService;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreBackupService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.mania.ManiaService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.server.VPinStudioServer.Features;

/**
 * Synchronizes the running VPin Mania Discord challenges of the current user into competitions of type
 * {@link CompetitionType#MANIA_CHALLENGE}, so that the matching tables get the competition id and optionally the
 * VPin Mania badge on their wheel icon.
 */
@Service
public class ManiaChallengeCompetitionSynchronizer implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaChallengeCompetitionSynchronizer.class);

  public static final String BADGE = "mania";
  public static final String OWNER = "VPin Mania";

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private CompetitionLifecycleService competitionLifecycleService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private GameService gameService;

  @Autowired
  private FrontendStatusService frontendStatusService;

  @Autowired
  private CompetitionIdUpdater competitionIdUpdater;

  @Autowired
  private ManiaService maniaService;

  @Autowired
  private HighscoreBackupService highscoreBackupService;

  @Autowired
  private HighscoreService highscoreService;

  public synchronized boolean synchronizeChallenges(boolean forceReload) {
    try {
      LOG.info("------------------------------- MANIA CHALLENGES SYNC -----------------------------------------------");
      ManiaSettings maniaSettings = preferencesService.getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
      VPinManiaClient maniaClient = maniaService.getClient();
      if (Features.MANIA_ENABLED && maniaClient != null && !StringUtils.isEmpty(maniaSettings.getApiKey()) && !StringUtils.isEmpty(maniaSettings.getCabinetUuid())) {
        DiscordChallengesResponse response = maniaClient.getDiscordChallengeClient().getChallenges();
        if (response != null) {
          List<DiscordChallenge> challenges = getActiveChallenges(maniaClient, response);
          List<Competition> challengeCompetitions = competitionService.getManiaChallengeCompetitions();
          for (DiscordChallenge challenge : challenges) {
            LOG.info("------------------------------> Sync of {}", challenge.getTableName());
            synchronizeChallenge(challenge, challengeCompetitions, maniaSettings, forceReload);
          }

          runCompetitionCleanupCheck(challenges, challengeCompetitions);
          runGameCleanupCheck();
        }
        return true;
      }
    }
    catch (Exception e) {
      LOG.error("Failed to synchronize VPin Mania challenges: {}", e.getMessage(), e);
    }
    finally {
      LOG.info("------------------------------- /MANIA CHALLENGES SYNC ----------------------------------------------");
    }
    return false;
  }

  /**
   * Lightweight periodic check that only deletes the competitions of challenges that have been finished, canceled
   * or have passed their end date. The remote challenges are only fetched if there are challenge competitions at all.
   */
  public synchronized void runFinishedChallengesCheck() {
    try {
      List<Competition> challengeCompetitions = competitionService.getManiaChallengeCompetitions();
      if (challengeCompetitions.isEmpty()) {
        return;
      }

      List<String> activeChallengeIds = null;
      ManiaSettings maniaSettings = preferencesService.getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
      VPinManiaClient maniaClient = maniaService.getClient();
      if (Features.MANIA_ENABLED && maniaClient != null && !StringUtils.isEmpty(maniaSettings.getApiKey()) && !StringUtils.isEmpty(maniaSettings.getCabinetUuid())) {
        DiscordChallengesResponse response = maniaClient.getDiscordChallengeClient().getChallenges();
        if (response != null) {
          activeChallengeIds = getActiveChallenges(maniaClient, response).stream().map(c -> String.valueOf(c.getId())).toList();
        }
      }

      boolean deleted = false;
      Instant now = Instant.now();
      for (Competition challengeCompetition : challengeCompetitions) {
        boolean expired = challengeCompetition.getEndDate() != null && !challengeCompetition.getEndDate().isAfter(now);
        boolean finished = activeChallengeIds != null && !activeChallengeIds.contains(challengeCompetition.getUuid());
        if (expired || finished) {
          LOG.info("Removing finished VPin Mania challenge competition \"{}\"", challengeCompetition.getName());
          competitionService.delete(challengeCompetition.getId());
          deleted = true;
        }
      }

      if (deleted) {
        runGameCleanupCheck();
      }
    }
    catch (Exception e) {
      LOG.error("Failed to run VPin Mania finished challenges check: {}", e.getMessage(), e);
    }
  }

  private List<DiscordChallenge> getActiveChallenges(@NonNull VPinManiaClient maniaClient, @NonNull DiscordChallengesResponse response) {
    boolean participating = isParticipating(maniaClient);
    return response.getChallenges().stream()
        .filter(c -> participating && c.isRunning() && !isOptedOut(c, response.getOptOuts()))
        .collect(Collectors.toList());
  }

  /**
   * The participation in all challenges can be disabled in the user settings on the VPin Mania website.
   */
  private boolean isParticipating(@NonNull VPinManiaClient maniaClient) {
    try {
      User currentUser = maniaClient.getUserClient().getCurrentUser();
      return currentUser == null || currentUser.isParticipatingInDiscordChallenges();
    }
    catch (Exception e) {
      LOG.warn("Failed to load VPin Mania user settings: {}", e.getMessage());
    }
    return true;
  }

  private static boolean isOptedOut(@NonNull DiscordChallenge challenge, @NonNull List<DiscordChallengeOptOut> optOuts) {
    if (challenge.isOptedOut()) {
      return true;
    }
    return optOuts.stream().anyMatch(o -> challenge.getGuildId().equals(o.getGuildId()) && (o.isServerWide() || challenge.getChannelId().equals(o.getChannelId())));
  }

  private void runCompetitionCleanupCheck(List<DiscordChallenge> challenges, List<Competition> challengeCompetitions) {
    List<String> challengeIds = challenges.stream().map(c -> String.valueOf(c.getId())).toList();
    for (Competition challengeCompetition : challengeCompetitions) {
      if (!challengeIds.contains(challengeCompetition.getUuid())) {
        LOG.info("Removing outdated VPin Mania challenge competition \"{}\"", challengeCompetition.getName());
        competitionService.delete(challengeCompetition.getId());
      }
    }
  }

  private void runGameCleanupCheck() {
    //clean old competition ids
    List<Competition> challengeCompetitions = competitionService.getManiaChallengeCompetitions();
    List<Integer> games = frontendService.getCompetedGamesIds(CompetitionType.MANIA_CHALLENGE);
    for (int gameId : games) {
      runGameCleanupCheck(challengeCompetitions, gameId);
    }
  }

  private void runGameCleanupCheck(List<Competition> challengeCompetitions, int gameId) {
    TableDetails tableDetails = frontendService.getTableDetails(gameId);
    LOG.info("Running VPin Mania challenge id cleanup check for {}", tableDetails.getGameDisplayName());
    if (!competitionIdUpdater.isCompeted(tableDetails, challengeCompetitions, false)) {
      competitionIdUpdater.unsetTourneyId(tableDetails, CompetitionType.MANIA_CHALLENGE, gameId);
      LOG.info("Cleared {} competition id from {}", CompetitionType.MANIA_CHALLENGE, tableDetails.getGameDisplayName());
      frontendStatusService.deAugmentWheel(gameService.getGame(gameId));
    }
  }

  private void synchronizeChallenge(DiscordChallenge challenge, List<Competition> challengeCompetitions, ManiaSettings maniaSettings, boolean forceReload) {
    String challengeId = String.valueOf(challenge.getId());
    for (Competition competition : challengeCompetitions) {
      if (challengeId.equals(competition.getUuid())) {
        Game game = gameService.getGame(competition.getGameId());
        if (game == null || forceReload) {
          //run de-augmentation and id cleanup, the game is matched again
          competitionLifecycleService.notifyCompetitionDeleted(competition);
        }
        updateCompetition(competition, challenge, maniaSettings);
        return;
      }
    }

    Competition competition = new Competition();
    updateCompetition(competition, challenge, maniaSettings);
  }

  private void updateCompetition(@NonNull Competition competition, @NonNull DiscordChallenge challenge, @NonNull ManiaSettings maniaSettings) {
    int previousGameId = competition.getId() != null ? competition.getGameId() : -1;
    competition.setIssues(null);
    competition.setUrl(challenge.getMessageUrl());
    competition.setUuid(String.valueOf(challenge.getId()));
    competition.setName(challenge.getTableName());
    competition.setBadge(maniaSettings.isChallengeBadgeEnabled() ? BADGE : null);
    competition.setOwner(OWNER);
    competition.setVpsTableId(challenge.getVpsTableId());
    competition.setVpsTableVersionId(challenge.getVpsVersionId());
    competition.setType(CompetitionType.MANIA_CHALLENGE.name());
    competition.setStartDate(challenge.getStartsAt() != null ? challenge.getStartsAt().toInstant() : null);
    competition.setEndDate(challenge.getEndsAt() != null ? challenge.getEndsAt().toInstant() : null);
    competition.setHighscoreReset(maniaSettings.isChallengeResetHighscores());

    if (StringUtils.isEmpty(challenge.getVpsTableId())) {
      LOG.info("VPin Mania challenge \"{}\" has no VPS table id", challenge.getTableName());
      addIssue(competition, "The challenge has no VPS table assigned.");
      competition.setGameId(-1);
    }
    else {
      List<Game> gameMatches = findMatchingGames(challenge.getVpsTableId(), challenge.getVpsVersionId());
      if (gameMatches.isEmpty()) {
        LOG.info("No matching game found for VPin Mania challenge \"{}\"", challenge.getTableName());
        addIssue(competition, "No matching game found for challenge \"" + challenge.getTableName() + "\".");
        addIssue(competition, "Please check the VPS matching of the table or download the correct version.");
        competition.setGameId(-1);
      }
      else {
        Game game = gameMatches.getFirst();
        competition.setGameId(game.getId());
        LOG.info("Applying game \"{}\" for VPin Mania challenge \"{}\"", game.getGameDisplayName(), challenge.getTableName());

        //the highscore is only reset once, when the table starts participating in the challenge
        if (competition.isHighscoreReset() && previousGameId != game.getId()) {
          if (highscoreBackupService.backup(game) != null) {
            highscoreService.resetHighscore(game);
            LOG.info("Reset highscore of \"{}\" for VPin Mania challenge \"{}\"", game.getGameDisplayName(), challenge.getTableName());
          }
        }
      }
    }

    competitionService.save(competition);
    LOG.info("Saved {}", competition);
  }

  private List<Game> findMatchingGames(@NonNull String vpsTableId, String vpsVersionId) {
    List<Game> gameMatches = gameService.getGamesByVpsTableId(vpsTableId, vpsVersionId);
    List<Game> filteredMatches = gameMatches.stream().filter(g -> {
      TableDetails tableDetails = frontendService.getTableDetails(g.getId());
      return tableDetails != null && tableDetails.getStatus() > 0;
    }).collect(Collectors.toList());

    filteredMatches.sort((o1, o2) -> o2.getModified().compareTo(o1.getModified()));
    return filteredMatches;
  }

  private static void addIssue(@NonNull Competition competition, @NonNull String msg) {
    String issue = competition.getIssues() != null ? competition.getIssues() : "";
    if (!issue.isEmpty()) {
      issue += "|";
    }
    issue += msg;
    competition.setIssues(issue);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
