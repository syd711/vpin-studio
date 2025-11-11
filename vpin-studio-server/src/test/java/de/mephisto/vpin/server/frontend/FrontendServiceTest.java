package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.frontend.popper.PinUPConnector;
import de.mephisto.vpin.server.games.Game;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FrontendServiceTest extends AbstractVPinServerTest {

  @Autowired
  private PinUPConnector connector;


  @Test
  public void testPupPlayerDisplays() {
    List<FrontendPlayerDisplay> pupPlayerDisplays = connector.getPupPlayerDisplays();
    assertFalse(pupPlayerDisplays.isEmpty());
  }

  @Test
  public void testGameAdding() {
    int emuId = 1;
    int count = connector.getGameCount(emuId);

    String baseName = FilenameUtils.getBaseName(EM_TABLE.getName());

    TableDetails tableDetails = new TableDetails();
    tableDetails.setEmulatorId(emuId);
    tableDetails.setStatus(1);
    tableDetails.setGameName(baseName);
    tableDetails.setGameFileName(baseName);
    tableDetails.setGameDisplayName(baseName);
    tableDetails.setDateModified(new java.util.Date());
    int l = connector.importGame(tableDetails);

    assertEquals(count + 1, connector.getGameCount(emuId));
    if (l > 0) {
      assertTrue(connector.deleteGame(l));
    }
    assertEquals(count, connector.getGameCount(emuId));
  }

  @Test
  public void testTableInfo() {
    int emuId = 1;

    Game gameByFilename = connector.getGameByFilename(emuId, EM_TABLE_NAME);
    TableDetails tableDetails = connector.getTableDetails(gameByFilename.getId());
    String uuid = UUID.randomUUID().toString();
    tableDetails.setAuthor(uuid);
    connector.saveTableDetails(gameByFilename.getId(), tableDetails);
    TableDetails tableDetailsUpdated = connector.getTableDetails(gameByFilename.getId());
    assertEquals(tableDetailsUpdated.getAuthor(), uuid);
  }

  @Test
  public void testConnector() {
    int emuId = 1;

    List<Game> games = connector.getGames();
    assertFalse(games.isEmpty());

    List<Integer> gameIds = connector.getGameIds(1);
    assertFalse(gameIds.isEmpty());

    Game game = connector.getGame(games.get(0).getId());
    assertNotNull(game);

    Game gameByFilename = connector.getGameByFilename(emuId, game.getGameFile().getName());
    assertNotNull(gameByFilename);

    assertNotEquals(connector.getGameCount(1), 0);

    assertNotNull(connector.getStartupScript());

    assertNotNull(connector.getEmulatorExitScript("Visual Pinball X"));
    assertNotNull(connector.getEmulatorExitScript("Future Pinball"));
    assertNotNull(connector.getEmulatorStartupScript("Visual Pinball X"));
    assertNotNull(connector.getEmulatorStartupScript("Future Pinball"));
  }

}
