package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.games.GameMediaService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompetitionIdUpdaterTest {

  @Mock
  private CompetitionLifecycleService competitionLifecycleService;
  @Mock
  private DiscordService discordService;
  @Mock
  private GameMediaService gameMediaService;
  @Mock
  private GameService gameService;
  @Mock
  private PreferencesService preferencesService;

  @InjectMocks
  private CompetitionIdUpdater competitionIdUpdater;

  // --- isCompeted ---

  @Test
  void isCompeted_returnsFalse_whenTourneyIdIsNull() {
    TableDetails td = new TableDetails();
    td.setTourneyId(null);

    assertFalse(competitionIdUpdater.isCompeted(td, Collections.emptyList(), true));
  }

  @Test
  void isCompeted_returnsFalse_whenTourneyIdIsEmpty() {
    TableDetails td = new TableDetails();
    td.setTourneyId("");

    assertFalse(competitionIdUpdater.isCompeted(td, Collections.emptyList(), true));
  }

  @Test
  void isCompeted_returnsTrue_whenTourneyIdMatchesLocalCompetition() {
    Competition competition = new Competition();
    competition.setType("SUBSCRIPTION");
    String expectedId = CompetitionIdFactory.createId(competition, true);

    TableDetails td = new TableDetails();
    td.setTourneyId(expectedId);

    assertTrue(competitionIdUpdater.isCompeted(td, Collections.singletonList(competition), true));
  }

  @Test
  void isCompeted_returnsTrue_whenTourneyIdMatchesRemoteCompetition() {
    Competition competition = new Competition();
    competition.setType("WEEKLY");
    String expectedId = CompetitionIdFactory.createId(competition, false);

    TableDetails td = new TableDetails();
    td.setTourneyId(expectedId);

    assertTrue(competitionIdUpdater.isCompeted(td, Collections.singletonList(competition), false));
  }

  @Test
  void isCompeted_returnsFalse_whenTourneyIdDoesNotMatchAnyCompetition() {
    Competition competition = new Competition();
    competition.setType("SUBSCRIPTION");

    TableDetails td = new TableDetails();
    td.setTourneyId("vps://competition/subscription/local/9999");

    // competition uses default id (null -> 0), so the id won't be 9999
    assertFalse(competitionIdUpdater.isCompeted(td, Collections.singletonList(competition), true));
  }

  @Test
  void isCompeted_returnsFalse_whenNoCompetitions() {
    TableDetails td = new TableDetails();
    td.setTourneyId("vps://competition/weekly/local/42");

    assertFalse(competitionIdUpdater.isCompeted(td, Collections.emptyList(), true));
  }

  // --- unsetTourneyId(TableDetails, CompetitionType, int) ---

  @Test
  void unsetTourneyId_removesEntriesContainingCompetitionType() {
    TableDetails td = new TableDetails();
    td.setTourneyId("weekly-something,iscored-entry");
    competitionIdUpdater.unsetTourneyId(td, CompetitionType.WEEKLY, 1);

    String updated = td.getTourneyId();
    assertFalse(updated.toLowerCase().contains("weekly"));
    assertTrue(updated.contains("iscored-entry"));
  }

  @Test
  void unsetTourneyId_keepsAllEntries_whenNoneMatchType() {
    TableDetails td = new TableDetails();
    td.setTourneyId("iscored-something,offline-entry");
    competitionIdUpdater.unsetTourneyId(td, CompetitionType.WEEKLY, 1);

    String updated = td.getTourneyId();
    assertTrue(updated.contains("iscored-something"));
    assertTrue(updated.contains("offline-entry"));
  }

  @Test
  void unsetTourneyId_handlesBlankSegments() {
    TableDetails td = new TableDetails();
    td.setTourneyId(",,,");
    competitionIdUpdater.unsetTourneyId(td, CompetitionType.WEEKLY, 1);

    assertEquals("", td.getTourneyId());
  }

  // --- afterPropertiesSet ---

  @Test
  void afterPropertiesSet_registersListenerWithLifecycleService() throws Exception {
    competitionIdUpdater.afterPropertiesSet();

    verify(competitionLifecycleService).addCompetitionChangeListener(competitionIdUpdater);
  }
}
