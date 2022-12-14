package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.server.VPinServerTest;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.players.Player;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CompetitionsTest extends VPinServerTest {

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private GameService gameService;

  @Test
  public void testCompetitions() {
    Game game = gameService.getGameByFilename(VPinServerTest.TEST_GAME_FILENAME);

    competitionService.addCompetitionChangeListener(new CompetitionChangeListener() {
      @Override
      public void competitionCreated(@NonNull Competition competition) {
        assertNotNull(competition);
      }

      @Override
      public void competitionChanged(@NonNull Competition competition) {
        assertNotNull(competition);
      }

      @Override
      public void competitionFinished(@NonNull Competition competition, @Nullable Player player) {
        assertNotNull(competition);
      }

      @Override
      public void competitionDeleted(@NonNull Competition competition) {
        assertNotNull(competition);
      }
    });

    Competition competition = new Competition();
    competition.setGameId(game.getId());
    competition.setName(String.valueOf(new Date().getTime()));
    competition.setStartDate(new Date());
    competition.setEndDate(new Date());

    Competition save = competitionService.save(competition);
    assertNotNull(save);
    assertFalse(save.isActive());
    assertNotNull(save.getCreatedAt());

    boolean delete = competitionService.delete(save.getId());
    assertTrue(delete);
  }
}
