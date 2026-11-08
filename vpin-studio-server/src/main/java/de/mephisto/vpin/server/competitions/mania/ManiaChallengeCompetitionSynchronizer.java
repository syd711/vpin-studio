package de.mephisto.vpin.server.competitions.mania;

import de.mephisto.vpin.server.util.ServerMessages;
import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.connectors.mania.model.DiscordChallenge;
import de.mephisto.vpin.connectors.mania.model.DiscordChallengeOptOut;
import de.mephisto.vpin.connectors.mania.model.DiscordChallengesResponse;
import de.mephisto.vpin.connectors.mania.model.DiscordServer;
import de.mephisto.vpin.connectors.mania.model.User;
import de.mephisto.vpin.restclient.mania.ManiaChallengeEntry;
import de.mephisto.vpin.restclient.mania.ManiaChallengeParticipation;
import de.mephisto.vpin.restclient.mania.ManiaChallengesResult;
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
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
    return doSynchronize(forceReload) != null;
  }

  /**
   * Synchronizes the challenges and returns them together with the state of their competitions. The result is built
   * from the same VPin Mania response the synchronization used, so it shows exactly what has been synchronized.
   */
  public synchronized ManiaChallengesResult getChallenges(boolean forceReload) {
    ManiaChallengesResult result = new ManiaChallengesResult();
    SyncData syncData = doSynchronize(forceReload);
    if (syncData == null || syncData.response() == null) {
      result.setError("VPin Mania challenges could not be loaded, check the server log for details.");
      return result;
    }

    DiscordChallengesResponse response = syncData.response();
    result.setParticipating(syncData.participating());
    if (response.getOptOuts() != null) {
      result.setOptOuts(response.getOptOuts());
    }

    try {
      List<DiscordServer> servers = syncData.maniaClient().getDiscordChallengeClient().getServers();
      if (servers != null) {
        result.setServers(servers);
      }
    }
    catch (Exception e) {
      // only used to resolve the server and channel names
      LOG.warn("Failed to load Discord servers of VPin Mania user: {}", e.getMessage());
    }

    List<Competition> challengeCompetitions = competitionService.getManiaChallengeCompetitions();
    for (DiscordChallenge challenge : response.getChallenges()) {
      ManiaChallengeEntry entry = new ManiaChallengeEntry();
      entry.setChallenge(challenge);

      String challengeId = String.valueOf(challenge.getId());
      Optional<Competition> competition = challengeCompetitions.stream().filter(c -> challengeId.equals(c.getUuid())).findFirst();
      if (competition.isPresent()) {
        // the competition is the source of truth, its game has the competition id and so is part of the playlist
        entry.setCompetitionId(competition.get().getId());
        entry.setGameId(competition.get().getGameId());
        entry.setIssues(competition.get().getIssues());
      }
      else if (!StringUtils.isEmpty(challenge.getVpsTableId())) {
        // inactive challenge, the game is only resolved for the display
        List<Game> matches = findMatchingGames(challenge.getVpsTableId(), challenge.getVpsVersionId());
        if (!matches.isEmpty()) {
          entry.setGameId(matches.getFirst().getId());
        }
      }

      if (entry.getGameId() < 0) {
        entry.setVersionMismatchGameId(findVersionMismatchGameId(challenge));
      }
      result.getChallenges().add(entry);
    }
    return result;
  }

  /**
   * Opts in or out of the challenges of a Discord server or channel and synchronizes the challenges right away,
   * so that the tables of the affected challenges are updated immediately.
   */
  public synchronized ManiaChallengesResult updateParticipation(@NonNull ManiaChallengeParticipation participation) throws Exception {
    VPinManiaClient maniaClient = maniaService.getClient();
    if (maniaClient == null) {
      throw new IllegalStateException("VPin Mania client not available");
    }

    if (participation.isOptOut()) {
      maniaClient.getDiscordChallengeClient().optOut(participation.getGuildId(), participation.getChannelId());
    }
    else {
      maniaClient.getDiscordChallengeClient().optIn(participation.getGuildId(), participation.getChannelId());
    }
    LOG.info("Updated VPin Mania challenge participation for guild {}, channel {}: opt-out={}", participation.getGuildId(), participation.getChannelId(), participation.isOptOut());
    return getChallenges(false);
  }

  private record SyncData(@NonNull VPinManiaClient maniaClient, @Nullable DiscordChallengesResponse response, boolean participating) {
  }

  /**
   * @return null if VPin Mania is not available or the synchronization failed
   */
  @Nullable
  private SyncData doSynchronize(boolean forceReload) {
    try {
      LOG.info("------------------------------- MANIA CHALLENGES SYNC -----------------------------------------------");
      ManiaSettings maniaSettings = preferencesService.getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
      VPinManiaClient maniaClient = maniaService.getClient();
      if (Features.MANIA_ENABLED && maniaClient != null && !StringUtils.isEmpty(maniaSettings.getApiKey()) && !StringUtils.isEmpty(maniaSettings.getCabinetUuid())) {
        DiscordChallengesResponse response = maniaClient.getDiscordChallengeClient().getChallenges();
        boolean participating = true;
        if (response != null) {
          participating = isParticipating(maniaClient);
          List<DiscordChallenge> challenges = getActiveChallenges(response, participating);
          List<Competition> challengeCompetitions = competitionService.getManiaChallengeCompetitions();
          for (DiscordChallenge challenge : challenges) {
            LOG.info("------------------------------> Sync of {}", challenge.getTableName());
            synchronizeChallenge(challenge, challengeCompetitions, maniaSettings, forceReload);
          }

          runCompetitionCleanupCheck(challenges, challengeCompetitions);
          runGameCleanupCheck();
        }
        return new SyncData(maniaClient, response, participating);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to synchronize VPin Mania challenges: {}", e.getMessage(), e);
    }
    finally {
      LOG.info("------------------------------- /MANIA CHALLENGES SYNC ----------------------------------------------");
    }
    return null;
  }

  /**
   * The server only assigns games of the exact table version of a challenge, as the scores of other versions do not
   * count for it. Another installed version is reported, so that the user can fix the VPS mapping or install the table.
   */
  private int findVersionMismatchGameId(@NonNull DiscordChallenge challenge) {
    if (StringUtils.isEmpty(challenge.getVpsTableId()) || StringUtils.isEmpty(challenge.getVpsVersionId())) {
      return -1;
    }
    return gameService.getGamesByVpsTableId(challenge.getVpsTableId(), null).stream()
        .filter(g -> !challenge.getVpsVersionId().equals(g.getExtTableVersionId()))
        .map(Game::getId)
        .findFirst()
        .orElse(-1);
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
          activeChallengeIds = getActiveChallenges(response, isParticipating(maniaClient)).stream().map(c -> String.valueOf(c.getId())).toList();
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

  private List<DiscordChallenge> getActiveChallenges(@NonNull DiscordChallengesResponse response, boolean participating) {
    Instant now = Instant.now();
    return response.getChallenges().stream()
        .filter(c -> participating && c.isRunning() && !isExpired(c, now) && !isOptedOut(c, response.getOptOuts()))
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

  /**
   * The challenge state on VPin Mania may lag behind its end date, so the end date is checked too.
   */
  private static boolean isExpired(@NonNull DiscordChallenge challenge, @NonNull Instant now) {
    return challenge.getEndsAt() != null && !challenge.getEndsAt().toInstant().isAfter(now);
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
      addIssue(competition, ServerMessages.msg("competition.issue.no_vps_table"));
      competition.setGameId(-1);
    }
    else {
      List<Game> gameMatches = findMatchingGames(challenge.getVpsTableId(), challenge.getVpsVersionId());
      if (gameMatches.isEmpty()) {
        LOG.info("No matching game found for VPin Mania challenge \"{}\"", challenge.getTableName());
        addIssue(competition, ServerMessages.msg("competition.issue.no_matching_game", challenge.getTableName()));
        addIssue(competition, ServerMessages.msg("competition.issue.check_vps_matching"));
        competition.setGameId(-1);
      }
      else {
        Game game = gameMatches.getFirst();
        competition.setGameId(game.getId());
        LOG.info("Applying game \"{}\" for VPin Mania challenge \"{}\"", game.getGameDisplayName(), challenge.getTableName());

        //the highscore is only reset once per challenge, when a table is matched for the first time,
        //so a table that drops out of the matching and comes back (or a re-match on another table) keeps its scores.
        //Competitions matched before this flag existed have previousGameId == game id and are only marked as done.
        if (competition.isHighscoreReset() && !competition.isHighscoreResetDone() && previousGameId != game.getId()) {
          if (highscoreBackupService.backup(game) != null) {
            highscoreService.resetHighscore(game);
            LOG.info("Reset highscore of \"{}\" for VPin Mania challenge \"{}\"", game.getGameDisplayName(), challenge.getTableName());
          }
        }
        competition.setHighscoreResetDone(true);
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
