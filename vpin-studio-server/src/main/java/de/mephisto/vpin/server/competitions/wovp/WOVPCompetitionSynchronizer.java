package de.mephisto.vpin.server.competitions.wovp;

import de.mephisto.vpin.connectors.wovp.Wovp;
import de.mephisto.vpin.connectors.wovp.models.*;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.tagging.TaggingUtil;
import de.mephisto.vpin.restclient.wovp.WOVPSettings;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionLifecycleService;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.vpx.VPXUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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
  private GameService gameService;

  public synchronized boolean synchronizeWovp(String apiKey, boolean forceReload) {
    try {
      LOG.info("------------------------------- WOVP SYNC -----------------------------------------------------------");
      WOVPSettings wovpSettings = preferencesService.getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);
      if (!StringUtils.isEmpty(apiKey) && wovpSettings.isEnabled()) {
        Wovp wovp = Wovp.create(apiKey);
        Challenges challenges = wovp.getChallenges(true);
        if (challenges != null) {
          List<Competition> weeklyCompetitions = competitionService.getWeeklyCompetitions();
          for (Challenge challenge : challenges.getItems()) {
            LOG.info("------------------------------> Sync of {}", challenge.getName());
            synchronizeChallenge(challenge, weeklyCompetitions, wovpSettings, forceReload);
          }
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
          competition.setEndDate(new Date());
          competitionService.save(competition);
          competitionLifecycleService.notifyCompetitionDeleted(competition);
          refreshTags(game, wovpSettings, false);

          updateCompetition(competition, challenge, wovpSettings);
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
    competition.setBadge(wovpSettings.isBadgeEnabled() ? "wovp" : null);
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
    competition.setStartDate(challenge.getStartDateUTC());
    competition.setEndDate(challenge.getEndDateUTC());
    competition.setMode(challenge.getChallengeTypeCode().name());
    competition.setHighscoreReset(wovpSettings.isResetHighscores());

    PinballTable pinballTable = challenge.getPinballTable();
    PinballTableVersion pinballTableVersion = challenge.getPinballTableVersion();
    String vpsVersion = pinballTableVersion != null ? pinballTableVersion.getExternalId() : null;
    List<Game> gameMatches = gameService.getGamesByVpsTableId(pinballTable.getExternalId(), vpsVersion);
    if (gameMatches.isEmpty()) {
      LOG.info("No matching game found for weekly challenge \"{}\"", challenge.getChallengeTypeCode());
      addIssue(competition, "No matching game found for weekly challenge \"" + getModeName(challenge) + "\".");
      addIssue(competition, "Please check the VPS matching of the table or download the correct version.");
      competition.setGameId(-1);
    }
    else {
      for (Game gameMatch : gameMatches) {
        LOG.info("Found matching wovp game for {}: {} / {} / {}", challenge.getName(), gameMatch.getGameDisplayName(), gameMatch.getExtTableId(), gameMatch.getExtTableVersionId());
      }

      Game game = gameMatches.get(0);
      List<String> scriptMatchKeywords = challenge.getScriptMatchKeywords();
      if (validateGameScript(game, scriptMatchKeywords)) {
        LOG.info("WOVP game validation successful, found phrase \"{}\" in VPX file {}", scriptMatchKeywords, game.getGameFileName());
        competition.setGameId(game.getId());
        refreshTags(game, wovpSettings, true);
        LOG.info("Applying game \"{}\" for weekly challenge \"{}\"", game.getGameDisplayName(), challenge.getChallengeTypeCode());
      }
      else {
        LOG.warn("WOVP game validation failed, did not find phrase \"{}\" in {}", scriptMatchKeywords, game.getGameFileName());
        addIssue(competition, "WOVP game validation failed, required phrases not found in " + game.getGameFileName() + ", invalid table version.");
        addIssue(competition, "Please check the VPS matching of the existing table or download the correct version.");
      }
    }

    competitionService.save(competition);
    LOG.info("Saved {}", competition);
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
          tableDetails.setTags(String.join(",", tagList));
          frontendService.saveTableDetails(game.getId(), tableDetails);
        }
      }
    }
  }

  private String getModeName(Challenge challenge) {
    if (challenge.getChallengeTypeCode().name().equals("tournament")) {
      return "KO";
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
