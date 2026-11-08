package de.mephisto.vpin.server.competitions.mania;

import de.mephisto.vpin.connectors.mania.DiscordChallengeClient;
import de.mephisto.vpin.connectors.mania.UserClient;
import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.connectors.mania.model.DiscordChallenge;
import de.mephisto.vpin.connectors.mania.model.DiscordChallengesResponse;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.mania.ManiaChallengeEntry;
import de.mephisto.vpin.restclient.mania.ManiaChallengesResult;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManiaChallengeCompetitionSynchronizerTest {

  @Mock
  private PreferencesService preferencesService;
  @Mock
  private CompetitionService competitionService;
  @Mock
  private CompetitionLifecycleService competitionLifecycleService;
  @Mock
  private FrontendService frontendService;
  @Mock
  private GameService gameService;
  @Mock
  private FrontendStatusService frontendStatusService;
  @Mock
  private CompetitionIdUpdater competitionIdUpdater;
  @Mock
  private ManiaService maniaService;
  @Mock
  private HighscoreBackupService highscoreBackupService;
  @Mock
  private HighscoreService highscoreService;

  @InjectMocks
  private ManiaChallengeCompetitionSynchronizer synchronizer;

  private ManiaSettings buildSettings(boolean registered, boolean badgeEnabled) {
    ManiaSettings settings = new ManiaSettings();
    if (registered) {
      settings.setApiKey("key");
      settings.setCabinetUuid("uuid");
    }
    settings.setChallengeBadgeEnabled(badgeEnabled);
    when(preferencesService.getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class)).thenReturn(settings);
    return settings;
  }

  private DiscordChallengeClient mockChallengeClient(DiscordChallenge... challenges) {
    VPinManiaClient maniaClient = mock(VPinManiaClient.class);
    DiscordChallengeClient challengeClient = mock(DiscordChallengeClient.class);
    UserClient userClient = mock(UserClient.class);
    DiscordChallengesResponse response = new DiscordChallengesResponse();
    response.setChallenges(new ArrayList<>(List.of(challenges)));

    when(maniaService.getClient()).thenReturn(maniaClient);
    lenient().when(maniaClient.getDiscordChallengeClient()).thenReturn(challengeClient);
    lenient().when(maniaClient.getUserClient()).thenReturn(userClient);
    lenient().when(challengeClient.getChallenges()).thenReturn(response);
    return challengeClient;
  }

  private static DiscordChallenge challenge(long id, String status) {
    DiscordChallenge challenge = new DiscordChallenge();
    challenge.setId(id);
    challenge.setGuildId("guild");
    challenge.setChannelId("channel");
    challenge.setTableName("Table " + id);
    challenge.setVpsTableId("vps" + id);
    challenge.setStatus(status);
    challenge.setStartsAt(new Date(System.currentTimeMillis() - 1000));
    challenge.setEndsAt(new Date(System.currentTimeMillis() + 100000));
    return challenge;
  }

  @Test
  void synchronizeChallenges_returnsFalse_whenNotRegistered() {
    buildSettings(false, true);
    mockChallengeClient();

    assertFalse(synchronizer.synchronizeChallenges(false));
    verifyNoInteractions(competitionService);
  }

  @Test
  void synchronizeChallenges_createsCompetitionWithBadge_forRunningChallenge() {
    buildSettings(true, true);
    mockChallengeClient(challenge(1, DiscordChallenge.STATUS_RUNNING), challenge(2, DiscordChallenge.STATUS_FINISHED));
    when(competitionService.getManiaChallengeCompetitions()).thenReturn(Collections.emptyList());
    when(gameService.getGamesByVpsTableId(anyString(), any())).thenReturn(new ArrayList<>());

    assertTrue(synchronizer.synchronizeChallenges(false));

    ArgumentCaptor<Competition> captor = ArgumentCaptor.forClass(Competition.class);
    verify(competitionService, times(1)).save(captor.capture());
    Competition saved = captor.getValue();
    assertEquals("1", saved.getUuid());
    assertEquals(CompetitionType.MANIA_CHALLENGE.name(), saved.getType());
    assertEquals(ManiaChallengeCompetitionSynchronizer.BADGE, saved.getBadge());
    assertEquals(-1, saved.getGameId());
  }

  @Test
  void synchronizeChallenges_deletesCompetition_ofFinishedChallenge() {
    buildSettings(true, false);
    mockChallengeClient(challenge(1, DiscordChallenge.STATUS_FINISHED));
    Competition outdated = new Competition();
    outdated.setId(5L);
    outdated.setUuid("1");
    when(competitionService.getManiaChallengeCompetitions()).thenReturn(List.of(outdated));

    assertTrue(synchronizer.synchronizeChallenges(false));

    verify(competitionService).delete(5L);
    verify(competitionService, never()).save(any());
  }

  @Test
  void runFinishedChallengesCheck_skipsRemoteCall_whenNoCompetitions() {
    when(competitionService.getManiaChallengeCompetitions()).thenReturn(Collections.emptyList());

    synchronizer.runFinishedChallengesCheck();

    verifyNoInteractions(maniaService);
    verify(competitionService, never()).delete(anyLong());
  }

  @Test
  void runFinishedChallengesCheck_deletesOnlyCompetitionsOfFinishedChallenges() {
    buildSettings(true, false);
    mockChallengeClient(challenge(1, DiscordChallenge.STATUS_RUNNING), challenge(2, DiscordChallenge.STATUS_FINISHED));
    Competition running = new Competition();
    running.setId(5L);
    running.setUuid("1");
    Competition finished = new Competition();
    finished.setId(6L);
    finished.setUuid("2");
    when(competitionService.getManiaChallengeCompetitions()).thenReturn(List.of(running, finished));

    synchronizer.runFinishedChallengesCheck();

    verify(competitionService).delete(6L);
    verify(competitionService, never()).delete(5L);
    verify(competitionService, never()).save(any());
  }

  @Test
  void runFinishedChallengesCheck_deletesExpiredCompetition_whenNotRegistered() {
    buildSettings(false, false);
    when(maniaService.getClient()).thenReturn(mock(VPinManiaClient.class));
    Competition expired = new Competition();
    expired.setId(5L);
    expired.setUuid("1");
    expired.setEndDate(Instant.now().minusSeconds(60));
    when(competitionService.getManiaChallengeCompetitions()).thenReturn(List.of(expired));

    synchronizer.runFinishedChallengesCheck();

    verify(competitionService).delete(5L);
  }

  // --- highscore reset ---

  private Game matchGame(int id) {
    Game game = mock(Game.class);
    lenient().when(game.getId()).thenReturn(id);
    TableDetails tableDetails = new TableDetails();
    tableDetails.setStatus(1);
    when(gameService.getGamesByVpsTableId(eq("vps1"), any())).thenReturn(new ArrayList<>(List.of(game)));
    when(frontendService.getTableDetails(id)).thenReturn(tableDetails);
    return game;
  }

  private Competition existingCompetition(int gameId, boolean resetDone) {
    Competition competition = new Competition();
    competition.setId(5L);
    competition.setUuid("1");
    competition.setGameId(gameId);
    competition.setHighscoreResetDone(resetDone);
    when(competitionService.getManiaChallengeCompetitions()).thenReturn(List.of(competition));
    return competition;
  }

  @Test
  void synchronizeChallenges_resetsHighscore_onFirstMatch() {
    buildSettings(true, false).setChallengeResetHighscores(true);
    mockChallengeClient(challenge(1, DiscordChallenge.STATUS_RUNNING));
    when(competitionService.getManiaChallengeCompetitions()).thenReturn(Collections.emptyList());
    Game game = matchGame(3);
    when(highscoreBackupService.backup(game)).thenReturn(new File("backup"));

    assertTrue(synchronizer.synchronizeChallenges(false));

    verify(highscoreService).resetHighscore(game);
    ArgumentCaptor<Competition> captor = ArgumentCaptor.forClass(Competition.class);
    verify(competitionService).save(captor.capture());
    assertTrue(captor.getValue().isHighscoreResetDone());
  }

  @Test
  void synchronizeChallenges_doesNotResetHighscore_whenTableIsMatchedAgain() {
    buildSettings(true, false).setChallengeResetHighscores(true);
    mockChallengeClient(challenge(1, DiscordChallenge.STATUS_RUNNING));
    // the table dropped out of the matching (e.g. disabled) after the reset had been applied
    existingCompetition(-1, true);
    matchGame(3);

    assertTrue(synchronizer.synchronizeChallenges(false));

    verifyNoInteractions(highscoreBackupService, highscoreService);
  }

  @Test
  void synchronizeChallenges_marksLegacyCompetitionAsResetDone_withoutReset() {
    buildSettings(true, false).setChallengeResetHighscores(true);
    mockChallengeClient(challenge(1, DiscordChallenge.STATUS_RUNNING));
    Competition competition = existingCompetition(3, false);
    matchGame(3);

    assertTrue(synchronizer.synchronizeChallenges(false));

    verifyNoInteractions(highscoreBackupService, highscoreService);
    assertTrue(competition.isHighscoreResetDone());
  }

  // --- getChallenges ---

  @Test
  void getChallenges_returnsError_whenNotRegistered() {
    buildSettings(false, false);
    mockChallengeClient();

    ManiaChallengesResult result = synchronizer.getChallenges(false);

    assertNotNull(result.getError());
    assertTrue(result.getChallenges().isEmpty());
  }

  @Test
  void getChallenges_returnsCompetitionStateOfActiveChallenge() {
    buildSettings(true, false);
    mockChallengeClient(challenge(1, DiscordChallenge.STATUS_RUNNING));
    Competition competition = existingCompetition(3, true);
    competition.setIssues("issue");
    matchGame(3);

    ManiaChallengesResult result = synchronizer.getChallenges(false);

    assertNull(result.getError());
    assertEquals(1, result.getChallenges().size());
    ManiaChallengeEntry entry = result.getChallenges().getFirst();
    assertEquals(5L, entry.getCompetitionId());
    assertEquals(3, entry.getGameId());
    assertEquals(-1, entry.getVersionMismatchGameId());
  }

  @Test
  void getChallenges_reportsOtherTableVersion_withoutAssigningIt() {
    buildSettings(true, false);
    DiscordChallenge challenge = challenge(1, DiscordChallenge.STATUS_RUNNING);
    challenge.setVpsVersionId("v2");
    mockChallengeClient(challenge);
    when(competitionService.getManiaChallengeCompetitions()).thenReturn(Collections.emptyList());
    Game otherVersion = mock(Game.class);
    when(otherVersion.getId()).thenReturn(4);
    when(otherVersion.getExtTableVersionId()).thenReturn("v1");
    when(gameService.getGamesByVpsTableId("vps1", "v2")).thenReturn(new ArrayList<>());
    when(gameService.getGamesByVpsTableId("vps1", null)).thenReturn(new ArrayList<>(List.of(otherVersion)));

    ManiaChallengesResult result = synchronizer.getChallenges(false);

    ArgumentCaptor<Competition> captor = ArgumentCaptor.forClass(Competition.class);
    verify(competitionService).save(captor.capture());
    assertEquals(-1, captor.getValue().getGameId());

    ManiaChallengeEntry entry = result.getChallenges().getFirst();
    assertEquals(-1, entry.getGameId());
    assertEquals(4, entry.getVersionMismatchGameId());
  }
}
