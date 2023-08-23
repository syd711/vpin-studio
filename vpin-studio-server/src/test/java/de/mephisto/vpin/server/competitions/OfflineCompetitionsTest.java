package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OfflineCompetitionsTest extends AbstractVPinServerTest {

  @Test
  public void testCompetitions() {
    Competition save = super.createOfflineCompetition(AbstractVPinServerTest.EM_TABLE_NAME);
    assertNotNull(save);
    assertFalse(save.isActive());
    assertNotNull(save.getCreatedAt());
    assertNotNull(save.getEndDate());
    assertNotNull(save.getStartDate());
    assertNotNull(save.getName());

    Competition finished = competitionService.finishCompetition(save);

    assertNotNull(finished.getWinnerInitials());
    assertTrue(competitionService.delete(save.getId()));
  }
}
