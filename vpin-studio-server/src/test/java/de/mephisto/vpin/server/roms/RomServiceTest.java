package de.mephisto.vpin.server.roms;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.games.Game;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RomServiceTest extends AbstractVPinServerTest {

  @Autowired
  private RomService romService;

  @BeforeAll
  public void setup() {
    setupSystem();
  }

  @Test
  public void testScanEmTable() {
    Game game = gameService.getGameByFilename(1, EM_TABLE_NAME);
    assertNotNull(game, "EM table not found");

    ScanResult result = romService.scanGameFile(game);
    assertNotNull(result);
    assertNotNull(result.getRom());
    assertEquals(EM_ROM_NAME, result.getRom());
  }

  @Test
  public void testScanVpregTable() {
    Game game = gameService.getGameByFilename(1, VPREG_TABLE_NAME);
    assertNotNull(game, "VPReg table not found");

    ScanResult result = romService.scanGameFile(game);
    assertNotNull(result);
    assertNotNull(result.getRom());
    assertEquals(VPREG_ROM_NAME, result.getRom());
  }

  @Test
  public void testScanNvramTable() {
    Game game = gameService.getGameByFilename(1, NVRAM_TABLE_NAME);
    assertNotNull(game, "NVRAM table not found");

    ScanResult result = romService.scanGameFile(game);
    assertNotNull(result);
    assertNotNull(result.getRom());
    assertEquals(NVRAM_ROM_NAME, result.getRom());
  }

  @Test
  public void testScanAllTables() {
    for (int i = 0; i < TABLE_NAMES.size(); i++) {
      Game game = gameService.getGameByFilename(1, TABLE_NAMES.get(i));
      assertNotNull(game, "Game not found: " + TABLE_NAMES.get(i));

      ScanResult result = romService.scanGameFile(game);
      assertNotNull(result, "Scan result null for: " + TABLE_NAMES.get(i));
      assertNotNull(result.getRom(), "ROM null for: " + TABLE_NAMES.get(i));
      assertEquals(ROM.get(i), result.getRom(), "ROM mismatch for: " + TABLE_NAMES.get(i));
    }
  }
}
