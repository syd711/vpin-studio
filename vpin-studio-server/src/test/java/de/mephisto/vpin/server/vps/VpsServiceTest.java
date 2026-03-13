package de.mephisto.vpin.server.vps;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.games.Game;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class VpsServiceTest extends AbstractVPinServerTest {

  @Autowired
  private VpsService vpsService;

  @BeforeAll
  public void setup() {
    setupSystem();
  }

  @Test
  public void testGetTables() {
    List<VpsTable> tables = vpsService.getTables();
    assertNotNull(tables);
  }

  @Test
  public void testAutoMatch() {
    Game game = gameService.getGameByFilename(1, EM_TABLE_NAME);
    assertNotNull(game);

    // autoMatch should not throw, result may be null if no match found
    vpsService.autoMatch(game, false);
  }

  @Test
  public void testAutoMatchAllTables() {
    for (String tableName : TABLE_NAMES) {
      Game game = gameService.getGameByFilename(1, tableName);
      assertNotNull(game, "Game not found: " + tableName);
      vpsService.autoMatch(game, false);
    }
  }

  @Test
  public void testFind() {
    List<VpsTable> results = vpsService.find("Twister", null);
    assertNotNull(results);
  }

  @Test
  public void testFindByRom() {
    List<VpsTable> results = vpsService.find("", NVRAM_ROM_NAME);
    assertNotNull(results);
  }

  @Test
  public void testGetTableByIdNull() {
    VpsTable table = vpsService.getTableById(null);
    assertNull(table);
  }

  @Test
  public void testGetTableByIdNonExistent() {
    VpsTable table = vpsService.getTableById("nonexistent_id_12345");
    assertNull(table);
  }

  @Test
  public void testGetChangeDate() {
    // may be null if never updated, but should not throw
    vpsService.getChangeDate();
  }
}
