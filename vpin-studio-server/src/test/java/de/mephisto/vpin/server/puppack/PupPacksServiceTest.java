package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.games.Game;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PupPacksServiceTest extends AbstractVPinServerTest {

  @Autowired
  private PupPacksService pupPacksService;

  @BeforeAll
  public void setup() {
    setupSystem();
  }

  @Test
  public void testGetPupPack() {
    for (String tableName : TABLE_NAMES) {
      Game game = gameService.getGameByFilename(1, tableName);
      assertNotNull(game, "Game not found: " + tableName);

      PupPack pupPack = pupPacksService.getPupPack(game);
      // pupPack may be null if no pup pack exists for this game
    }
  }

  @Test
  public void testGetPupPackCached() {
    for (String tableName : TABLE_NAMES) {
      Game game = gameService.getGameByFilename(1, tableName);
      assertNotNull(game, "Game not found: " + tableName);

      PupPack cached = pupPacksService.getPupPackCached(game);
      // cached may be null
    }
  }

  @Test
  public void testHasPupPack() {
    for (String tableName : TABLE_NAMES) {
      Game game = gameService.getGameByFilename(1, tableName);
      assertNotNull(game);

      // should not throw
      pupPacksService.hasPupPack(game);
    }
  }

  @Test
  public void testIsPupPackDisabled() {
    for (String tableName : TABLE_NAMES) {
      Game game = gameService.getGameByFilename(1, tableName);
      assertNotNull(game);

      // should not throw
      pupPacksService.isPupPackDisabled(game);
    }
  }

  @Test
  public void testGetMenuPupPack() {
    PupPack menuPack = pupPacksService.getMenuPupPack();
    // may be null if no menu pup pack exists
  }

  @Test
  public void testClearCache() {
    boolean result = pupPacksService.clearCache();
    assertTrue(result);
  }

  @Test
  public void testGetPupPackConsistency() {
    Game game = gameService.getGameByFilename(1, NVRAM_TABLE_NAME);
    assertNotNull(game);

    PupPack pack1 = pupPacksService.getPupPack(game);
    PupPack pack2 = pupPacksService.getPupPackCached(game);

    // If both return non-null, the folder should be the same
    if (pack1 != null && pack2 != null) {
      assertEquals(pack1.getPupPackFolder(), pack2.getPupPackFolder());
    }
  }
}
