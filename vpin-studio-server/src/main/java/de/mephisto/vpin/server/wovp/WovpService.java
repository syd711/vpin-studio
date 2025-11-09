package de.mephisto.vpin.server.wovp;

import de.mephisto.vpin.connectors.wovp.Wovp;
import de.mephisto.vpin.connectors.wovp.models.Challenge;
import de.mephisto.vpin.connectors.wovp.models.ChallengeTypeCode;
import de.mephisto.vpin.connectors.wovp.models.Challenges;
import de.mephisto.vpin.connectors.wovp.models.PinballTable;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WovpService implements InitializingBean, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(WovpService.class);

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private GameService gameService;

  private WOVPSettings wovpSettings;


  public String validateKey() {
    String apiKey = wovpSettings.getApiKey();
    Wovp wovp = Wovp.create(apiKey);
    return wovp.validateKey();
  }


  /**
   * The WOVP service does not need to return a specific value for the sync result.
   * The weekly competitions should remain abstract from the specific competition organizer.
   *
   * @throws Exception
   */
  public void synchronize() throws Exception {
    String apiKey = wovpSettings.getApiKey();
    if (!StringUtils.isEmpty(apiKey)) {
      Wovp wovp = Wovp.create(apiKey);
      Challenges challenges = wovp.getChallenges();
      if (challenges != null) {
        List<Competition> weeklyCompetitions = competitionService.getWeeklyCompetitions();
        for (Challenge challenge : challenges.getItems()) {
          synchronizeChallenge(challenge, weeklyCompetitions);
        }
      }
    }
  }

  private void synchronizeChallenge(Challenge challenge, List<Competition> weeklyCompetitions) {
    String challengeId = challenge.getId();
    ChallengeTypeCode challengeTypeCode = challenge.getChallengeTypeCode();
    for (Competition competition : weeklyCompetitions) {
      if (challengeTypeCode.name().equals(competition.getType())) {
        Game game = gameService.getGame(competition.getGameId());
        if (!challengeId.equals(competition.getUuid()) || game == null) {
          updateCompetition(competition, challenge);
        }
        return;
      }
    }

    Competition competition = new Competition();
    updateCompetition(competition, challenge);
  }

  private void updateCompetition(@NonNull Competition competition, @NonNull Challenge challenge) {
    competition.setUrl(challenge.getPinballTable().getBackglassImage().getMediumVariant().getUrl());
    competition.setUuid(challenge.getId());
    competition.setName(challenge.getName());
    competition.setVpsTableId(challenge.getPinballTable().getExternalId());
    competition.setType(CompetitionType.WEEKLY.name());
    competition.setStartDate(challenge.getStartDateUTC());
    competition.setEndDate(challenge.getEndDateUTC());
    competition.setType(challenge.getChallengeTypeCode().name());
    competition.setHighscoreReset(wovpSettings.isResetHighscores());

    PinballTable pinballTable = challenge.getPinballTable();
    List<Game> gameMatches = gameService.getGamesByVpsTableId(pinballTable.getExternalId(), null);
    if (gameMatches.isEmpty()) {
      LOG.info("No matching game found for weeky challenge \"{}\"", challenge.getName());
    }
    else {
      Game game = gameMatches.get(0);
      competition.setGameId(game.getId());


    }

    competitionService.save(competition);
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (PreferenceNames.WOVP_SETTINGS.equals(propertyName)) {
      wovpSettings = preferencesService.getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);
      synchronize();
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    wovpSettings = preferencesService.getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);
    preferencesService.addChangeListener(this);
    synchronize();
    LOG.info("Initialized {}", this.getClass().getSimpleName());
  }
}
