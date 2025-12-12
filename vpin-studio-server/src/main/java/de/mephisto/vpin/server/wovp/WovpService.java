package de.mephisto.vpin.server.wovp;

import de.mephisto.vpin.connectors.wovp.Wovp;
import de.mephisto.vpin.connectors.wovp.models.*;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionScore;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.restclient.wovp.ScoreSubmitResult;
import de.mephisto.vpin.restclient.wovp.WOVPSettings;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.competitions.wovp.WOVPCompetitionSynchronizer;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.GameStatusService;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreMetadata;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.listeners.EventOrigin;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.recorder.ScreenshotService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
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

  @Autowired
  private GameService gameService;

  @Autowired
  private GameStatusService gameStatusService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private ScreenshotService screenshotService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private PlayerService playerService;

  private WOVPSettings wovpSettings;

  public String validateKey() {
    String apiKey = wovpSettings.getApiKey();
    Wovp wovp = Wovp.create(apiKey);
    return wovp.validateKey();
  }

  public ScoreSubmitResult submitScore(boolean simulate) {
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

    String apiKey = wovpSettings.getApiKey();
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
    highscoreService.scanScore(game, EventOrigin.COMPETITION_UPDATE);
    ScoreSummary scoreSummary = highscoreService.getScoreSummary(-1, game);

    if (scoreSummary.getScores().isEmpty()) {
      SLOG.info("[WOVP simulate=" + simulate + "] " + "No highscores found for this game.");
      result.setErrorMessage("No highscores found for this game.");
      return result;
    }

    Player adminPlayer = playerService.getAdminPlayer();
    if (adminPlayer == null) {
      SLOG.info("[WOVP simulate=" + simulate + "] " + "The system has no default player set.");
      result.setErrorMessage("The system has no default player set.");
      return result;
    }

    Optional<Score> score = scoreSummary.getScores().stream().filter(s -> s.getPlayer() != null && s.getPlayer().equals(adminPlayer)).findFirst();
    if (score.isEmpty()) {
      SLOG.info("[WOVP simulate=" + simulate + "] " + "No matching table score found for player " + adminPlayer.getInitials());
      result.setErrorMessage("No matching table score found for player " + adminPlayer.getInitials());
      return result;
    }

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
      challenges = wovp.getChallenges(false);
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

    File screenshotFile = screenshotService.getScreenshotFile(null);
    Challenge c = challenge.get();
    Score s = score.get();

    result.setScore(s.getScore());
    result.setPlayerInitials(adminPlayer.getInitials());
    result.setPlayerName(adminPlayer.getName());

    if (!simulate) {
      try {
        wovp.submitScore(screenshotFile, c.getId(), s.getScore(), getNote(game));
        SLOG.info("[WOVP simulate=" + simulate + "] " + "WOVP score submit finished. Submitted a score of " + s.getFormattedScore() + " for player " + c.getName());
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

  private String getNote(@NonNull Game game) {
    StringBuilder builder = new StringBuilder();
    builder.append("VPX File: ");
    builder.append(game.getGameFileName());
    builder.append("\n");
    builder.append("ROM: ");
    builder.append(game.getRom());
    builder.append("\n");
    if (!StringUtils.isEmpty(game.getRomAlias())) {
      builder.append("ROM Alias: ");
      builder.append(game.getRomAlias());
      builder.append("\n");
    }
    builder.append("Highscore Type: ");
    builder.append(game.getHighscoreType());
    builder.append("\n");
    return builder.toString();
  }

  public List<CompetitionScore> getWeeklyScores(@NonNull String uuid) {
    try {
      String apiKey = wovpSettings.getApiKey();
      Wovp wovp = Wovp.create(apiKey);
      Challenges challenges = wovp.getChallenges(true);
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

  public boolean isScoreSubmitEnabled() {
    wovpSettings = preferencesService.getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);
    ScoreSubmitResult scoreSubmitResult = submitScore(true);
    return scoreSubmitResult.getErrorMessage() == null;
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
