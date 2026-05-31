package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.server.players.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompetitionLifecycleServiceTest {

  @InjectMocks
  private CompetitionLifecycleService service;

  private CompetitionChangeListener listener;
  private Competition competition;

  @BeforeEach
  void setUp() {
    listener = mock(CompetitionChangeListener.class);
    competition = new Competition();
    service.addCompetitionChangeListener(listener);
  }

  @Test
  void notifyCompetitionCreation_invokesListener() {
    service.notifyCompetitionCreation(competition);
    verify(listener).competitionCreated(competition);
  }

  @Test
  void notifyCompetitionStarted_invokesListener() {
    service.notifyCompetitionStarted(competition);
    verify(listener).competitionStarted(competition);
  }

  @Test
  void notifyCompetitionChanged_invokesListener() {
    service.notifyCompetitionChanged(competition);
    verify(listener).competitionChanged(competition);
  }

  @Test
  void notifyCompetitionDeleted_invokesListener() {
    service.notifyCompetitionDeleted(competition);
    verify(listener).competitionDeleted(competition);
  }

  @Test
  void notifyCompetitionFinished_invokesListener() {
    Player player = new Player();
    ScoreSummary summary = new ScoreSummary();
    service.notifyCompetitionFinished(competition, player, summary);
    verify(listener).competitionFinished(competition, player, summary);
  }

  @Test
  void multipleListeners_allNotified() {
    CompetitionChangeListener second = mock(CompetitionChangeListener.class);
    service.addCompetitionChangeListener(second);

    service.notifyCompetitionCreation(competition);

    verify(listener).competitionCreated(competition);
    verify(second).competitionCreated(competition);
  }

  @Test
  void noListeners_doesNotThrow() {
    CompetitionLifecycleService fresh = new CompetitionLifecycleService();
    fresh.notifyCompetitionCreation(competition);
    fresh.notifyCompetitionStarted(competition);
    fresh.notifyCompetitionChanged(competition);
    fresh.notifyCompetitionDeleted(competition);
  }
}
