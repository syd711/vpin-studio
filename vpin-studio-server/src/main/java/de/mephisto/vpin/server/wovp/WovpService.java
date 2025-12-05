package de.mephisto.vpin.server.wovp;

import de.mephisto.vpin.connectors.wovp.Wovp;
import de.mephisto.vpin.connectors.wovp.models.*;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionScore;
import de.mephisto.vpin.restclient.wovp.WOVPSettings;
import de.mephisto.vpin.server.competitions.wovp.WOVPCompetitionSynchronizer;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WovpService implements InitializingBean, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(WovpService.class);

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private WOVPCompetitionSynchronizer wovpCompetitionSynchronizer;

  private WOVPSettings wovpSettings;
  private Wovp wovp;


  public String validateKey() {
    String apiKey = wovpSettings.getApiKey();
    wovp = Wovp.create(apiKey);
    return wovp.validateKey();
  }

  public List<CompetitionScore> getWeeklyScores(@NonNull String uuid) {
    try {
      String apiKey = wovpSettings.getApiKey();
      Wovp wovp = Wovp.create(apiKey);
      Challenges challenges = wovp.getChallenges();
      Optional<Challenge> first = challenges.getItems().stream().filter(c -> c.getId().equals(uuid)).findFirst();
      if (first.isPresent()) {
        Challenge challenge = first.get();
        List<ScoreBoardItem> items = challenge.getScoreBoard().getItems();
        return items.stream().map(item -> toCompetitionScore(challenge, item)).collect(Collectors.toList());
      }
    }
    catch (Exception e) {
      LOG.error("Failed to load weekly scores: {}", e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  private CompetitionScore toCompetitionScore(Challenge challenge, ScoreBoardItem item) {
    ScoreBoardItemPositionValues values = item.getValues();
    BackglassImage backglassImage = challenge.getPinballTable().getBackglassImage();
    String backgroundImageUrl = null;
    if (backglassImage != null) {
      backgroundImageUrl = backglassImage.getMediumVariant().getUrl();
    }

    CompetitionScore score = new CompetitionScore();
    score.setChallengeImageUrl(backgroundImageUrl);
    score.setFlagUrl("https://www.vpin-mania.net/flags/" + values.getParticipantCountryCode().toLowerCase() + ".png");
    score.setAvatarUrl(values.getParticipantProfilePicture());
    score.setLeague(values.getParticipantDivision());
    score.setRank(values.getPosition());
    score.setParticipantName(values.getParticipantName());
    score.setUserId(values.getUserId());
    score.setScore(values.getScore());
    score.setParticipantCountryCode(values.getParticipantCountryCode());
    score.setPlatform(values.getParticipantPlayingPlatform());
    score.setParticipantId(values.getParticipantId());
    score.setPending(values.isPending());
    score.setNote(values.getApprovalNote());
    return score;
  }

  /**
   * The WOVP service does not need to return a specific value for the sync result.
   * The weekly competitions should remain abstract from the specific competition organizer.
   */
  public boolean synchronize(boolean forceReload) {
    return wovpCompetitionSynchronizer.synchronizeWovp(forceReload);
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (PreferenceNames.WOVP_SETTINGS.equals(propertyName)) {
      wovpSettings = preferencesService.getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);
      new Thread(() -> {
        synchronize(false);
      }).start();
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    wovpSettings = preferencesService.getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);
    preferencesService.addChangeListener(this);
    new Thread(() -> {
      synchronize(false);
    }).start();
    LOG.info("Initialized {}", this.getClass().getSimpleName());
  }
}
