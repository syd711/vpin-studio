package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OfflineCompetitionsTest extends AbstractVPinServerTest {

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private GameService gameService;

  @Test
  public void testCompetitions() {
    Game game = null;// gameService.getGameByFilename(AbstractVPinServerTest.TEST_GAME_FILENAME);

    Competition competition = new Competition();
    competition.setGameId(game.getId());
    competition.setType(CompetitionType.OFFLINE.name());
    competition.setName(String.valueOf(new Date().getTime()));
    competition.setStartDate(new Date());
    competition.setEndDate(new Date());

    Competition save = competitionService.save(competition);
    assertNotNull(save);
    assertFalse(save.isActive());
    assertNotNull(save.getCreatedAt());

    Competition finished = competitionService.finishCompetition(competition);
    assertNotNull(finished.getWinnerInitials());

    boolean delete = competitionService.delete(save.getId());
    assertTrue(delete);
  }
}
