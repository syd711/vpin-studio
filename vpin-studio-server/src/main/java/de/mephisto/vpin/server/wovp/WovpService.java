package de.mephisto.vpin.server.wovp;

import de.mephisto.vpin.connectors.wovp.Wovp;
import de.mephisto.vpin.connectors.wovp.models.*;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionScore;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.restclient.wovp.ScoreSubmitResult;
import de.mephisto.vpin.restclient.wovp.WOVPSettings;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.wovp.WOVPCompetitionSynchronizer;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.GameStatusService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.recorder.ScreenshotService;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WovpService implements InitializingBean, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(WovpService.class);

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private WOVPCompetitionSynchronizer wovpCompetitionSynchronizer;

  @Autowired
  private GameService gameService;

  @Autowired
  private GameStatusService gameStatusService;

  @Autowired
  private ScreenshotService screenshotService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private SystemService systemService;

  private WOVPSettings wovpSettings;

  public ApiKeyValidationResponse validateKey(@Nullable String apiKey) {
    if (StringUtils.isEmpty(apiKey)) {
      ApiKeyValidationResponse response = new ApiKeyValidationResponse();
      response.setSuccess(false);
      return response;
    }

    Wovp wovp = Wovp.create(apiKey);
    return wovp.validateKey();
  }

  public WovpPlayer getPlayer(@NonNull String userId) {
    return Wovp.getPlayer(userId);
  }

  public ScoreSubmitResult submitScore(@NonNull WovpPlayer wovpPlayer, boolean simulate) {
    ScoreSubmitResult result = new ScoreSubmitResult();
    if (!wovpSettings.isEnabled()) {
      SLOG.info("[WOVP simulate=" + simulate + "] " + "WOVP not enabled");
      result.setErrorMessage("WOVP not enabled");
      return result;
    }

    if (!wovpSettings.isUseScoreSubmitter()) {
      SLOG.info("[WOVP simulate=" + simulate + "] " + "Score submitter not enabled");
      result.setErrorMessage("Score submitter not enabled");
      return result;
    }

    //the serialized player does not have the api key
    WovpPlayer cachedPlayer = getPlayer(wovpPlayer.getId());
    String apiKey = cachedPlayer.getApiKey();
    if (StringUtils.isEmpty(apiKey)) {
      SLOG.info("[WOVP simulate=" + simulate + "] " + "No API key set.");
      result.setErrorMessage("No API key set.");
      return result;
    }

    GameStatus status = gameStatusService.getStatus();
    if (status == null || status.getGameId() < 0) {
      SLOG.info("[WOVP simulate=" + simulate + "] " + "No active game found.");
      result.setErrorMessage("No active game found.");
      return result;
    }

    Game game = gameService.getGame(status.getGameId());
    List<Competition> weeklyCompetitions = competitionService.getWeeklyCompetitions();
    Optional<Competition> competition = weeklyCompetitions.stream().filter(c -> c.getGameId() == game.getId()).findFirst();
    if (competition.isEmpty()) {
      SLOG.info("[WOVP simulate=" + simulate + "] " + "No matching challenge found for this game.");
      result.setErrorMessage("No matching challenge found for this game.");
      return result;
    }


    Wovp wovp = Wovp.create(apiKey);
    Challenges challenges = null;
    try {
      challenges = wovp.getChallenges(true);
    }
    catch (Exception e) {
      SLOG.info("[WOVP simulate=" + simulate + "] " + "Fetching challenges failed: " + e.getMessage());
      result.setErrorMessage("Fetching challenges failed: " + e.getMessage());
      return result;
    }

    Optional<Challenge> challenge = challenges.getItems().stream().filter(c -> c.getId().equals(competition.get().getUuid())).findFirst();
    if (!challenge.isPresent()) {
      SLOG.info("[WOVP simulate=" + simulate + "] " + "Challenge synchronization failed.");
      result.setErrorMessage("Challenge synchronization failed.");
      return result;
    }

    Optional<ScoreBoardItem> first = challenge.get().getScoreBoard().getItems().stream().filter(s -> s.getValues().getUserId().equals(wovpPlayer.getId())).findFirst();
    if (first.isPresent()) {
      ScoreBoardItemPositionValues item = first.get().getValues();
      result.setLatestScore((long) item.getScore());
      result.setPlayerName(item.getParticipantName());
    }

    if (!simulate) {
      File screenshotFile = screenshotService.getScreenshotFile(null);
      if (screenshotFile == null || !screenshotFile.exists()) {
        SLOG.info("[WOVP simulate=" + simulate + "] " + "No matching screenshot found.");
        result.setErrorMessage("No matching screenshot found.");
        return result;
      }

      try {
        wovp.submitScore(screenshotFile, challenge.get().getId(), 0, getMetadata(game));
        LOG.info("[WOVP simulate=" + simulate + "] " + "WOVP score submit finished. Submitted a score for " + cachedPlayer.getName());
        SLOG.info("[WOVP simulate=" + simulate + "] " + "WOVP score submit finished. Submitted a score for " + cachedPlayer.getName());
      }
      catch (Exception e) {
        SLOG.info("[WOVP simulate=" + simulate + "] " + "Failed to submit WOVP highscore: " + e.getMessage());
        LOG.error("Failed to submit WOVP highscore: {}", e.getMessage(), e);
        result.setErrorMessage("Failed to submit WOVP highscore: " + e.getMessage());
        return result;
      }
    }

    return result;
  }

  private ScoreSubmitMetadata getMetadata(@NonNull Game game) {
    PauseMenuSettings pauseMenuSettings = preferencesService.getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);

    ScoreSubmitMetadata metadata = new ScoreSubmitMetadata();
    metadata.setVpinStudioVersion(systemService.getVersion());
    metadata.setVpxFile(game.getGameFileName());
    metadata.setRom(game.getRom());
    metadata.setPlatform(pauseMenuSettings.isDesktopUser() ? 1 : 0);
    return metadata;
  }

  public List<CompetitionScore> getWeeklyScores(@NonNull String uuid) {
    try {
      String anyApiKey = wovpSettings.getAnyApiKey();
      if (anyApiKey != null) {
        Wovp wovp = Wovp.create(anyApiKey);
        Challenges challenges = wovp.getChallenges(true);
        Optional<Challenge> first = challenges.getItems().stream().filter(c -> c.getId().equals(uuid)).findFirst();
        if (first.isPresent()) {
          Challenge challenge = first.get();
          List<ScoreBoardItem> items = challenge.getScoreBoard().getItems();
          return items.stream().map(item -> toCompetitionScore(challenge, item)).collect(Collectors.toList());
        }
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
    score.setMyScore(getPlayer(values.getUserId()) != null);
    return score;
  }

  public boolean isScoreSubmitEnabled() {
    wovpSettings = preferencesService.getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);
    List<WovpPlayer> players = Wovp.getPlayers();
    if (!players.isEmpty()) {
      ScoreSubmitResult scoreSubmitResult = submitScore(players.get(0), true);
      return scoreSubmitResult.getErrorMessage() == null;
    }
    return false;
  }

  /**
   * The WOVP service does not need to return a specific value for the sync result.
   * The weekly competitions should remain abstract from the specific competition organizer.
   */
  public boolean synchronize(boolean forceReload) {
    return wovpCompetitionSynchronizer.synchronizeWovp(wovpSettings.getAnyApiKey(), forceReload);
  }

  public List<WovpPlayer> getPlayers() {
    return Wovp.getPlayers();
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
      List<String> apiKeys = wovpSettings.getApiKeys();
      for (String apiKey : apiKeys) {
        validateKey(apiKey);
      }
      synchronize(false);
    }).start();
    LOG.info("Initialized {}", this.getClass().getSimpleName());
  }

  public boolean clearCache() {
    Wovp.clearCache();
    wovpSettings = preferencesService.getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);
    List<String> apiKeys = wovpSettings.getApiKeys();
    for (String apiKey : apiKeys) {
      validateKey(apiKey);
    }
    return true;
  }
}
