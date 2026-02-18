package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.highscores.ScoreList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CompetitionServiceTest extends AbstractVPinServerTest {

  @BeforeAll
  public void setup() {
    setupSystem();
  }

  @Test
  public void testSaveAndDelete() {
    Competition competition = null;
    try {
      competition = createOfflineCompetition(EM_TABLE_NAME);
      assertNotNull(competition);
      assertNotNull(competition.getId());
      assertNotNull(competition.getName());
      assertNotNull(competition.getStartDate());
      assertNotNull(competition.getEndDate());
      assertNotNull(competition.getCreatedAt());
    }
    finally {
      if (competition != null) {
        assertTrue(competitionService.delete(competition.getId()));
      }
    }
  }

  @Test
  public void testGetOfflineCompetitions() {
    Competition competition = null;
    try {
      competition = createOfflineCompetition(VPREG_TABLE_NAME);
      assertNotNull(competition);

      List<Competition> offlineCompetitions = competitionService.getOfflineCompetitions();
      assertNotNull(offlineCompetitions);
      assertFalse(offlineCompetitions.isEmpty());
    }
    finally {
      if (competition != null) {
        competitionService.delete(competition.getId());
      }
    }
  }

  @Test
  public void testGetCompetitionScores() {
    Competition competition = null;
    try {
      competition = createOfflineCompetition(EM_TABLE_NAME);
      assertNotNull(competition);

      ScoreList scores = competitionService.getCompetitionScores(competition.getId());
      assertNotNull(scores);
    }
    finally {
      if (competition != null) {
        competitionService.delete(competition.getId());
      }
    }
  }

  @Test
  public void testFinishCompetition() {
    Competition competition = null;
    try {
      competition = createOfflineCompetition(EM_TABLE_NAME);
      assertNotNull(competition);
      assertFalse(competition.isActive());

      Competition finished = competitionService.finishCompetition(competition);
      assertNotNull(finished);
      assertNotNull(finished.getWinnerInitials());
    }
    finally {
      if (competition != null) {
        competitionService.delete(competition.getId());
      }
    }
  }

  @Test
  public void testGetCompetitionById() {
    Competition competition = null;
    try {
      competition = createOfflineCompetition(NVRAM_TABLE_NAME);
      assertNotNull(competition);

      Competition fetched = competitionService.getCompetition(competition.getId());
      assertNotNull(fetched);
      assertEquals(competition.getId(), fetched.getId());
      assertEquals(competition.getName(), fetched.getName());
    }
    finally {
      if (competition != null) {
        competitionService.delete(competition.getId());
      }
    }
  }

  @Test
  public void testGetActiveCompetitions() {
    List<Competition> active = competitionService.getActiveCompetitions();
    assertNotNull(active);
  }

  @Test
  public void testGetFinishedCompetitions() {
    List<Competition> finished = competitionService.getFinishedCompetitions(10);
    assertNotNull(finished);
  }
}
