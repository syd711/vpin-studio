package de.mephisto.vpin.server.competitions.mania;

import de.mephisto.vpin.connectors.mania.DiscordChallengeClient;
import de.mephisto.vpin.connectors.mania.UserClient;
import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.connectors.mania.model.DiscordChallenge;
import de.mephisto.vpin.connectors.mania.model.DiscordChallengesResponse;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionIdUpdater;
import de.mephisto.vpin.server.competitions.CompetitionLifecycleService;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.mania.ManiaService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
