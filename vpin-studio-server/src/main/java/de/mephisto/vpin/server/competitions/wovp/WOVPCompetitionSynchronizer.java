package de.mephisto.vpin.server.competitions.wovp;

import de.mephisto.vpin.connectors.wovp.Wovp;
import de.mephisto.vpin.connectors.wovp.models.*;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.wovp.WOVPSettings;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
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

import java.util.List;

@Service
public class WOVPCompetitionSynchronizer implements InitializingBean, ApplicationListener<ApplicationReadyEvent>, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(WOVPCompetitionSynchronizer.class);

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private GameService gameService;

  public synchronized boolean synchronizeWovp(boolean forceReload) {
    try {
      WOVPSettings wovpSettings = preferencesService.getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);
      String apiKey = wovpSettings.getApiKey();
      if (!StringUtils.isEmpty(apiKey) && wovpSettings.isEnabled()) {
        Wovp wovp = Wovp.create(apiKey);
        Challenges challenges = wovp.getChallenges();
        if (challenges != null) {
          List<Competition> weeklyCompetitions = competitionService.getWeeklyCompetitions();
          for (Challenge challenge : challenges.getItems()) {
            synchronizeChallenge(challenge, weeklyCompetitions, wovpSettings, forceReload);
          }
        }
        return true;
      }
    }
    catch (Exception e) {
      LOG.error("Failed to synchronize with WOVP: {}", e.getMessage(), e);
    }
    return false;
  }


  private void synchronizeChallenge(Challenge challenge, List<Competition> weeklyCompetitions, WOVPSettings wovpSettings, boolean forceReload) {
    String challengeId = challenge.getId();
    ChallengeTypeCode challengeTypeCode = challenge.getChallengeTypeCode();
    for (Competition competition : weeklyCompetitions) {
      if (challengeTypeCode.name().equals(competition.getMode())) {
        Game game = gameService.getGame(competition.getGameId());
        //the id is not matching when it is outdated
        if (!challengeId.equals(competition.getUuid()) || game == null || forceReload) {
          updateCompetition(competition, challenge, wovpSettings, game);
        }
        return;
      }
    }

    Competition competition = new Competition();
    updateCompetition(competition, challenge, wovpSettings, null);
  }

  private void updateCompetition(@NonNull Competition competition, @NonNull Challenge challenge, WOVPSettings wovpSettings, @Nullable Game game) {
    competition.setUrl("https://worldofvirtualpinball.com/en/challenge/ranking?tab=challenges");
    competition.setUuid(challenge.getId());
    competition.setName(challenge.getName());
    competition.setOwner("World Of Virtual Pinball");
    competition.setVpsTableId(challenge.getPinballTable().getExternalId());
    competition.setVpsTableVersionId(challenge.getPinballTableVersion().getExternalId());
    competition.setType(CompetitionType.WEEKLY.name());
    competition.setStartDate(challenge.getStartDateUTC());
    competition.setEndDate(challenge.getEndDateUTC());
    competition.setMode(challenge.getChallengeTypeCode().name());
    competition.setHighscoreReset(wovpSettings.isResetHighscores());

    if (game == null) {
      PinballTable pinballTable = challenge.getPinballTable();
      PinballTableVersion pinballTableVersion = challenge.getPinballTableVersion();
      List<Game> gameMatches = gameService.getGamesByVpsTableId(pinballTable.getExternalId(), pinballTableVersion.getExternalId());
      if (gameMatches.isEmpty()) {
        LOG.info("No matching game found for weekly challenge \"{}\"", challenge.getChallengeTypeCode());
      }
      else {
        game = gameMatches.get(0);
        competition.setGameId(game.getId());
        LOG.info("Applying game \"{}\" for weekly challenge \"{}\"", game.getGameDisplayName(), challenge.getChallengeTypeCode());
      }
    }

    competitionService.save(competition);
    LOG.info("Saved {}", competition);
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    WOVPSettings settings = preferencesService.getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);
    if (settings.isEnabled()) {
      new Thread(() -> {
        long start = System.currentTimeMillis();
        Thread.currentThread().setName("Wovp Initial Sync");
        LOG.info("----------------------------- Initial WOVP Sync --------------------------------------------------");
        synchronizeWovp(false);
        LOG.info("----------------------------- /Initial WOVP Sync -------------------------------------------------");
        LOG.info("Initial sync finished, took {}ms", (System.currentTimeMillis() - start));
      }).start();
    }
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (PreferenceNames.WOVP_SETTINGS.equals(propertyName)) {
      synchronizeWovp(false);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
