package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionLifecycleService;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompetitionChangeListenerImplTest {

  @Mock
  private CompetitionService competitionService;
  @Mock
  private CompetitionLifecycleService competitionLifecycleService;
  @Mock
  private GameService gameService;
  @Mock
  private FrontendStatusService frontendStatusService;

  @InjectMocks
  private CompetitionChangeListenerImpl listener;

  private Competition activeCompetition;
  private Competition inactiveCompetition;
  private Game game;

  @BeforeEach
  void setup() {
    game = mock(Game.class);

    activeCompetition = new Competition();
    activeCompetition.setGameId(1);
    // WEEKLY type makes isActive() unconditionally true
    activeCompetition.setType(CompetitionType.WEEKLY.name());

    // Inactive: OFFLINE type with past end date
    inactiveCompetition = new Competition();
    inactiveCompetition.setGameId(1);
    inactiveCompetition.setType("OFFLINE");
    inactiveCompetition.setWinnerInitials("ABC"); // makes isActive() return false
  }

  @Test
  void competitionStarted_withBadgeAndActiveGame_augmentsWheel() {
    activeCompetition.setBadge("badge.png");
    when(gameService.getGame(1)).thenReturn(game);
    when(frontendStatusService.isWheelAugmented(game)).thenReturn(false);
    when(competitionService.getFinishedByDateCompetitions()).thenReturn(Collections.emptyList());

    listener.competitionStarted(activeCompetition);

    verify(frontendStatusService).augmentWheel(game, "badge.png");
  }

  @Test
  void competitionStarted_withBadgeAlreadyAugmented_doesNotReaugment() {
    activeCompetition.setBadge("badge.png");
    when(gameService.getGame(1)).thenReturn(game);
    when(frontendStatusService.isWheelAugmented(game)).thenReturn(true);
    when(competitionService.getFinishedByDateCompetitions()).thenReturn(Collections.emptyList());

    listener.competitionStarted(activeCompetition);

    verify(frontendStatusService, never()).augmentWheel(any(), any());
  }

  @Test
  void competitionStarted_withNoBadge_deAugmentsWheel_whenCurrentlyAugmented() {
    activeCompetition.setBadge(null);
    when(gameService.getGame(1)).thenReturn(game);
    when(frontendStatusService.isWheelAugmented(game)).thenReturn(true);
    when(competitionService.getFinishedByDateCompetitions()).thenReturn(Collections.emptyList());

    listener.competitionStarted(activeCompetition);

    verify(frontendStatusService).deAugmentWheel(game);
  }

  @Test
  void competitionStarted_withNoGame_skipsWheelOps() {
    activeCompetition.setBadge("badge.png");
    when(gameService.getGame(1)).thenReturn(null);
    when(competitionService.getFinishedByDateCompetitions()).thenReturn(Collections.emptyList());

    listener.competitionStarted(activeCompetition);

    verify(frontendStatusService, never()).augmentWheel(any(), any());
    verify(frontendStatusService, never()).deAugmentWheel(any());
  }

  @Test
  void competitionStarted_withInactiveCompetition_skipsWheelOps() {
    inactiveCompetition.setBadge("badge.png");
    when(gameService.getGame(1)).thenReturn(game);
    when(competitionService.getFinishedByDateCompetitions()).thenReturn(Collections.emptyList());

    listener.competitionStarted(inactiveCompetition);

    verify(frontendStatusService, never()).augmentWheel(any(), any());
    verify(frontendStatusService, never()).deAugmentWheel(any());
  }

  @Test
  void competitionChanged_delegatesToRefreshBadge() {
    activeCompetition.setBadge("new-badge.png");
    when(gameService.getGame(1)).thenReturn(game);
    when(frontendStatusService.isWheelAugmented(game)).thenReturn(false);
    when(competitionService.getFinishedByDateCompetitions()).thenReturn(Collections.emptyList());

    listener.competitionChanged(activeCompetition);

    verify(frontendStatusService).augmentWheel(game, "new-badge.png");
  }

  @Test
  void competitionDeleted_withGame_deAugmentsWheel() {
    when(gameService.getGame(1)).thenReturn(game);

    listener.competitionDeleted(activeCompetition);

    verify(frontendStatusService).deAugmentWheel(game);
  }

  @Test
  void competitionDeleted_withNoGame_looksByVpsTable() {
    Competition competition = new Competition();
    competition.setGameId(0);
    competition.setType(CompetitionType.WEEKLY.name());
    competition.setVpsTableId("vps-123");
    competition.setVpsTableVersionId("v1");
    when(gameService.getGame(0)).thenReturn(null);
    when(gameService.getGameByVpsTable("vps-123", "v1")).thenReturn(game);

    listener.competitionDeleted(competition);

    verify(frontendStatusService).deAugmentWheel(game);
  }

  @Test
  void competitionDeleted_withNoGameAnywhere_skipsDeAugment() {
    Competition competition = new Competition();
    competition.setGameId(1);
    competition.setType(CompetitionType.WEEKLY.name());
    when(gameService.getGame(1)).thenReturn(null);
    when(gameService.getGameByVpsTable(any(), any())).thenReturn(null);

    listener.competitionDeleted(competition);

    verify(frontendStatusService, never()).deAugmentWheel(any());
  }

  @Test
  void afterPropertiesSet_registersListener() throws Exception {
    listener.afterPropertiesSet();
    verify(competitionLifecycleService).addCompetitionChangeListener(listener);
  }
}
