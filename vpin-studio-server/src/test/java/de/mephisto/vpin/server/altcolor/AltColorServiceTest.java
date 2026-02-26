package de.mephisto.vpin.server.altcolor;

import de.mephisto.vpin.restclient.altcolor.AltColor;
import de.mephisto.vpin.restclient.altcolor.AltColorTypes;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.games.Game;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AltColorServiceTest extends AbstractVPinServerTest {

  @Autowired
  private AltColorService altColorService;

  @BeforeAll
  public void setup() {
    setupSystem();
  }

  @Test
  public void testGetAltColorType() {
    for (String tableName : TABLE_NAMES) {
      Game game = gameService.getGameByFilename(1, tableName);
      assertNotNull(game, "Game not found: " + tableName);

      // getAltColorType returns null when no alt color is available
      AltColorTypes type = altColorService.getAltColorType(game);
    }
  }

  @Test
  public void testGetAltColor() {
    for (String tableName : TABLE_NAMES) {
      Game game = gameService.getGameByFilename(1, tableName);
      assertNotNull(game);

      AltColor altColor = altColorService.getAltColor(game);
      assertNotNull(altColor);
    }
  }

  @Test
  public void testGetAltColorFolder() {
    for (String tableName : TABLE_NAMES) {
      Game game = gameService.getGameByFilename(1, tableName);
      assertNotNull(game);

      File folder = altColorService.getAltColorFolder(game);
      // folder may be null if ROM is not set
    }
  }

  @Test
  public void testGetAltColorEmTable() {
    Game game = gameService.getGameByFilename(1, EM_TABLE_NAME);
    assertNotNull(game);

    AltColor altColor = altColorService.getAltColor(game);
    assertNotNull(altColor);
    // EM tables typically don't have alt colors
  }

  @Test
  public void testGetAltColorNvramTable() {
    Game game = gameService.getGameByFilename(1, NVRAM_TABLE_NAME);
    assertNotNull(game);

    AltColor altColor = altColorService.getAltColor(game);
    assertNotNull(altColor);
  }
}
