package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.invoke.MethodHandles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OfflineCompetitionsTest extends AbstractVPinServerTest {
    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Test
  public void testCompetitions() {
    try {
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
    catch (Exception e) {
      LOG.error("OfflineCompetitionsTest failed: {}", e.getMessage(), e);
    }
  }
}
