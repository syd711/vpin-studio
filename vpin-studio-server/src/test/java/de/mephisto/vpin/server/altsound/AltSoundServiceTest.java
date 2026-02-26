package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.games.Game;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AltSoundServiceTest extends AbstractVPinServerTest {

  @Autowired
  private AltSoundService altSoundService;

  @BeforeAll
  public void setup() {
    setupSystem();
  }

  @Test
  public void testIsAltSoundAvailable() {
    for (String tableName : TABLE_NAMES) {
      Game game = gameService.getGameByFilename(1, tableName);
      assertNotNull(game, "Game not found: " + tableName);

      // should not throw, returns true/false
      altSoundService.isAltSoundAvailable(game);
    }
  }

  @Test
  public void testGetAltSoundFolder() {
    for (String tableName : TABLE_NAMES) {
      Game game = gameService.getGameByFilename(1, tableName);
      assertNotNull(game);

      File folder = altSoundService.getAltSoundFolder(game);
      // folder may be null if ROM is not set
    }
  }

  @Test
  public void testGetAltSound() {
    for (String tableName : TABLE_NAMES) {
      Game game = gameService.getGameByFilename(1, tableName);
      assertNotNull(game);

      AltSound altSound = altSoundService.getAltSound(game);
      // altSound may be null if no alt sound files exist
    }
  }

  @Test
  public void testIsAltSoundAvailableEmTable() {
    Game game = gameService.getGameByFilename(1, EM_TABLE_NAME);
    assertNotNull(game);

    boolean available = altSoundService.isAltSoundAvailable(game);
    // EM tables typically don't have alt sounds
  }

  @Test
  public void testIsAltSoundAvailableNvramTable() {
    Game game = gameService.getGameByFilename(1, NVRAM_TABLE_NAME);
    assertNotNull(game);

    boolean available = altSoundService.isAltSoundAvailable(game);
  }

  @Test
  public void testClearCache() {
    boolean result = altSoundService.clearCache();
    assertTrue(result);
  }
}
