package de.mephisto.vpin.server.competitions.wovp;

import de.mephisto.vpin.server.util.ServerMessages;
import de.mephisto.vpin.connectors.mania.CabinetClient;
import de.mephisto.vpin.connectors.wovp.Wovp;
import de.mephisto.vpin.connectors.wovp.models.*;
import de.mephisto.vpin.connectors.wovp.patcher.WovpPatchStatus;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.tagging.TaggingUtil;
import de.mephisto.vpin.restclient.wovp.WOVPSettings;
import de.mephisto.vpin.restclient.wovp.WovpPatchResult;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionIdUpdater;
import de.mephisto.vpin.server.competitions.CompetitionLifecycleService;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.frontend.TableStatusChangeListener;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameCachingService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.GameStatusService;
import de.mephisto.vpin.server.highscores.HighscoreBackupService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.vpx.VPXUtil;
import de.mephisto.vpin.server.wovp.WovpPatchService;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WOVPCompetitionSynchronizer implements InitializingBean, ApplicationListener<ApplicationReadyEvent>, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(WOVPCompetitionSynchronizer.class);

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private CompetitionLifecycleService competitionLifecycleService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private HighscoreBackupService highscoreBackupService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private GameService gameService;

  @Autowired
  private FrontendStatusService frontendStatusService;

  @Autowired
  private CompetitionIdUpdater competitionIdUpdater;

  @Autowired
  private GameCachingService gameCachingService;

  @Autowired
  private WovpPatchService wovpPatchService;

  @Autowired
  private GameStatusService gameStatusService;

  public synchronized boolean synchronizeWovp(String apiKey, boolean forceReload) {
    try {
      LOG.info("------------------------------- WOVP SYNC -----------------------------------------------------------");
      WOVPSettings wovpSettings = preferencesService.getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);
      if (!StringUtils.isEmpty(apiKey) && wovpSettings.isEnabled()) {
        Wovp wovp = Wovp.create(apiKey);
        Challenges challenges = wovp.getChallenges(forceReload);
        if (challenges != null) {
          List<Competition> weeklyCompetitions = competitionService.getWeeklyCompetitions();
          for (Challenge challenge : challenges.getItems()) {
            LOG.info("------------------------------> Sync of {}", challenge.getName());
            try {
              synchronizeChallenge(challenge, weeklyCompetitions, wovpSettings, forceReload);
            }
            catch (Exception e) {
              LOG.error("Failed to synchronize WOVP challenge \"{}\": {}", challenge.getName(), e.getMessage(), e);
            }
          }

          runCompetitionCleanupCheck(challenges, weeklyCompetitions, wovpSettings);
          runGameCleanupCheck(wovpSettings);
          runTelemetryPatchCheck(wovpSettings);
        }
        return true;
      }
    }
    catch (Exception e) {
      LOG.error("Failed to synchronize with WOVP: {}", e.getMessage(), e);
    }
    finally {
      LOG.info("------------------------------- /WOVP SYNC ----------------------------------------------------------");
    }
    return false;
  }

  private void runCompetitionCleanupCheck(Challenges challenges, List<Competition> weeklyCompetitions, WOVPSettings wovpSettings) {
    List<String> challengesIds = challenges.getItems().stream().map(c -> c.getId()).toList();
    for (Competition weeklyCompetition : weeklyCompetitions) {
      String uuid = weeklyCompetition.getUuid();
      if (!challengesIds.contains(uuid)) {
        competitionService.delete(weeklyCompetition.getId());
        applyTelemetryPatch(gameService.getGame(weeklyCompetition.getGameId()), wovpSettings, false);
      }
    }
  }

  private void runGameCleanupCheck(WOVPSettings wovpSettings) {
    //clean old competition ids
    List<Competition> weeklyCompetitions = competitionService.getWeeklyCompetitions();
    List<Integer> games = frontendService.getCompetedGamesIds(CompetitionType.WEEKLY);
    for (int gameId : games) {
      runGameCleanupCheck(weeklyCompetitions, gameId, wovpSettings);
    }
  }

  private void runGameCleanupCheck(List<Competition> weeklyCompetitions, int gameId, WOVPSettings wovpSettings) {
    TableDetails tableDetails = frontendService.getTableDetails(gameId);
    LOG.info("Running WOVP tournament id cleanup check for {}", tableDetails.getGameDisplayName());
    if (!competitionIdUpdater.isCompeted(tableDetails, weeklyCompetitions, false)) {
      competitionIdUpdater.unsetTourneyId(tableDetails, CompetitionType.WEEKLY, gameId);
      LOG.info("Cleared {} competition id from {}", CompetitionType.WEEKLY, tableDetails.getGameDisplayName());
      Game game = gameService.getGame(gameId);
      frontendStatusService.deAugmentWheel(game);
      applyTelemetryPatch(game, wovpSettings, false);
    }
  }

  /**
   * Ensures that the tables of all weekly competitions contain the WOVP telemetry.
   * This runs as a separate pass, so that it also applies to existing competitions, e.g. after the option has been enabled.
   */
  private void runTelemetryPatchCheck(WOVPSettings wovpSettings) {
    if (!wovpSettings.isAutoPatchTables()) {
      return;
    }
    for (Competition competition : competitionService.getWeeklyCompetitions()) {
      if (competition.getGameId() > 0) {
        applyTelemetryPatch(gameService.getGame(competition.getGameId()), wovpSettings, true);
      }
    }
  }

  /**
   * Adds or removes the WOVP telemetry of the given table. A backup of the table is created before it is modified.
   * The telemetry is only removed when the table is not used by any other weekly competition.
   */
  private void applyTelemetryPatch(@Nullable Game game, @NonNull WOVPSettings wovpSettings, boolean add) {
    if (game == null || !wovpSettings.isAutoPatchTables() || !game.isVpxGame()) {
      return;
    }
    String action = add ? "patch" : "removal";
    try {
      if (!add && competitionService.getWeeklyCompetitions().stream().anyMatch(c -> c.getGameId() == game.getId())) {
        return;
      }
      if (wovpPatchService.isPatched(game.getId()) == add) {
        return;
      }
      GameStatus status = gameStatusService.getStatus();
      if (status != null && status.getGameId() == game.getId()) {
        LOG.info("Skipped WOVP telemetry {} for \"{}\", the table is currently played.", action, game.getGameDisplayName());
        return;
      }

      WovpPatchResult result = add ? wovpPatchService.patch(game.getId(), true) : wovpPatchService.unpatch(game.getId(), true);
      if (result.getStatus() == WovpPatchStatus.FAILED) {
        LOG.warn("WOVP telemetry {} for \"{}\" failed: {}", action, game.getGameDisplayName(), result.getMessage());
      }
      else {
        LOG.info("WOVP telemetry {} for \"{}\" finished: {}", action, game.getGameDisplayName(), result.getStatus());
      }
    }
    catch (Exception e) {
      LOG.error("WOVP telemetry {} for \"{}\" failed: {}", action, game.getGameDisplayName(), e.getMessage(), e);
    }
  }

  private void synchronizeChallenge(Challenge challenge, List<Competition> weeklyCompetitions, WOVPSettings wovpSettings, boolean forceReload) {
    String challengeId = challenge.getId();
    ChallengeTypeCode challengeTypeCode = challenge.getChallengeTypeCode();
    for (Competition competition : weeklyCompetitions) {
      //The competition ids and games might change, but the mode remains
      if (challengeTypeCode.name().equals(competition.getMode())) {
        Game game = gameService.getGame(competition.getGameId());
        //When the challenge id has changed, it means that the existing competition is outdated.
        if (!challengeId.equals(competition.getUuid()) || game == null || forceReload) {
          //run de-augmentation for finished competitions
          competition.setEndDate(Instant.now());
//          competitionService.save(competition);//TOOD required?
          competitionLifecycleService.notifyCompetitionDeleted(competition);
          refreshTags(game, wovpSettings, false);

          updateCompetition(competition, challenge, wovpSettings);
          if (game != null && competition.isHighscoreReset() && !challengeId.equals(competition.getUuid())) {
            if (highscoreBackupService.backup(game) != null) {
              highscoreService.resetHighscore(game);
            }
          }
          //the previous table is not competed anymore, unless it has been selected again
          applyTelemetryPatch(game, wovpSettings, false);
        }
        return;
      }
    }

    Competition competition = new Competition();
    updateCompetition(competition, challenge, wovpSettings);
  }

  private void updateCompetition(@NonNull Competition competition, @NonNull Challenge challenge, @NonNull WOVPSettings wovpSettings) {
    competition.setIssues(null);
    competition.setUrl("https://worldofvirtualpinball.com/en/challenge/ranking?tab=challenges");
    competition.setUuid(challenge.getId());
    competition.setName(challenge.getName());
    competition.setBadge(wovpSettings.isBadgeEnabled() ? getBadgeName(challenge) : null);
    competition.setOwner("World Of Virtual Pinball");
    competition.setVpsTableId(challenge.getPinballTable().getExternalId());

    String tableChallengeVersion = challenge.getPinballTableVersion().getExternalId();
    if (tableChallengeVersion != null) {
      competition.setVpsTableVersionId(tableChallengeVersion);
    }
    else {
      LOG.warn("WOVP did not set a VPS version id for challenge {}", challenge.getName());
      addIssue(competition, "WOVP did not set a VPS version id for this challenge.");
    }
    competition.setType(CompetitionType.WEEKLY.name());

    competition.setStartDate(challenge.getStartDateUTC().toInstant());
    competition.setEndDate(challenge.getEndDateUTC().toInstant());

    competition.setMode(challenge.getChallengeTypeCode().name());
    competition.setHighscoreReset(wovpSettings.isResetHighscores());

    PinballTable pinballTable = challenge.getPinballTable();
    PinballTableVersion pinballTableVersion = challenge.getPinballTableVersion();
    String vpsVersion = pinballTableVersion != null ? pinballTableVersion.getExternalId() : null;
    List<Game> gameMatches = findMatchingGame(pinballTable, vpsVersion);
    if (gameMatches.isEmpty()) {
      LOG.info("No matching game found for weekly challenge \"{}\"", challenge.getChallengeTypeCode());
      addIssue(competition, ServerMessages.msg("competition.issue.no_matching_weekly_game", getModeName(challenge)));
      addIssue(competition, ServerMessages.msg("competition.issue.check_vps_matching"));
      competition.setGameId(-1);
    }
    else {
      for (Game gameMatch : gameMatches) {
        LOG.info("Found matching wovp game for {}: {} / {} / {} / {}", challenge.getName(), gameMatch.getGameDisplayName(), gameMatch.getExtTableId(), gameMatch.getExtTableVersionId(), gameMatch.getModified());
      }

      Game game = gameMatches.getFirst();
      List<String> scriptMatchKeywords = challenge.getScriptMatchKeywords();
      if (validateGameScript(game, scriptMatchKeywords)) {
        LOG.info("WOVP game validation successful, found phrase \"{}\" in VPX file {}", scriptMatchKeywords, game.getGameFileName());
        competition.setGameId(game.getId());
        refreshTags(game, wovpSettings, true);
        LOG.info("Applying game \"{}\" for weekly challenge \"{}\"", game.getGameDisplayName(), challenge.getChallengeTypeCode());
      }
      else {
        competition.setGameId(-1);
        LOG.warn("WOVP game validation failed, did not find phrase \"{}\" in {}", scriptMatchKeywords, game.getGameFileName());
        addIssue(competition, ServerMessages.msg("competition.issue.wovp_validation_failed", game.getGameFileName()));
        addIssue(competition, ServerMessages.msg("competition.issue.check_vps_matching_existing"));
      }
    }

    competitionService.save(competition);
    LOG.info("Saved {}", competition);
  }

  private List<Game> findMatchingGame(PinballTable pinballTable, String vpsVersion) {
    List<Game> gameMatches = gameService.getGamesByVpsTableId(pinballTable.getExternalId(), vpsVersion);
    List<Game> filteredMatches = gameMatches.stream().filter(g -> {
      if (g.getGameFile() == null || !g.getGameFile().exists()) {
        LOG.warn("Skipping WOVP match candidate \"{}\", game file does not exist: {}", g.getGameDisplayName(), g.getGameFile());
        return false;
      }
      TableDetails tableDetails = frontendService.getTableDetails(g.getId());
      return tableDetails.getStatus() > 0;
    }).collect(Collectors.toList());

    filteredMatches.sort(Comparator.comparing(Game::getModified, Comparator.nullsLast(Comparator.reverseOrder())));
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

  private boolean validateGameScript(Game game, @Nullable List<String> terms) {
    if (terms != null) {
      String s = VPXUtil.readScript(game.getGameFile());
      for (String term : terms) {
        if (!s.contains(term)) {
          return false;
        }
      }
    }

    return true;
  }

  private void refreshTags(@Nullable Game game, @NonNull WOVPSettings wovpSettings, boolean add) {
    if (game != null) {
      if (wovpSettings.isTaggingEnabled()) {
        TableDetails tableDetails = frontendService.getTableDetails(game.getId());
        String tags = tableDetails.getTags();
        List<String> tagList = TaggingUtil.getTags(tags);
        List<String> weeklyTags = wovpSettings.getTags();
        boolean dirty = false;
        for (String weeklyTag : weeklyTags) {
          if (add && !tagList.contains(weeklyTag)) {
            tagList.add(weeklyTag);
            dirty = true;
          }
          else if (!add) {
            if (tagList.remove(weeklyTag)) {
              dirty = true;
            }
          }
        }

        if (dirty) {
          tableDetails.setTags(TaggingUtil.join(tagList));
          frontendService.saveTableDetails(game.getId(), tableDetails);
        }
      }
    }
  }

  private String getBadgeName(Challenge challenge) {
    switch (challenge.getChallengeTypeCode()) {
      case tournament: {
        return "wovp-ko";
      }
      case monthly_contracts: {
        return "wovp-contracts";
      }
      case extra: {
        return "wovp-extra";
      }
      case season:
      default: {
        return "wovp-main";
      }
    }
  }

  private String getModeName(Challenge challenge) {
    if (challenge.getChallengeTypeCode().name().equals("tournament")) {
      return "KO";
    }
    if (challenge.getChallengeTypeCode().name().equals("monthly_contracts")) {
      return "Monthly Contracts";
    }
    return StringUtils.capitalize(challenge.getChallengeTypeCode().name());
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    WOVPSettings settings = preferencesService.getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);
    if (settings.isEnabled()) {
      new Thread(() -> {
        long start = System.currentTimeMillis();
        Thread.currentThread().setName("Wovp Initial Sync");
        LOG.info("----------------------------- Initial WOVP Sync --------------------------------------------------");
        synchronizeWovp(settings.getAnyApiKey(), true);
        LOG.info("----------------------------- /Initial WOVP Sync -------------------------------------------------");
        LOG.info("Initial sync finished, took {}ms", (System.currentTimeMillis() - start));
        gameCachingService.clearCache();
      }).start();

    }
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (PreferenceNames.WOVP_SETTINGS.equals(propertyName)) {
      WOVPSettings settings = preferencesService.getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);
      synchronizeWovp(settings.getAnyApiKey(), false);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
