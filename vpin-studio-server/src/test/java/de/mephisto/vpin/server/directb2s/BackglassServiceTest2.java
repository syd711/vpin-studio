package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.Game;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BackglassServiceTest2 extends AbstractVPinServerTest {

  @Autowired
  private BackglassService backglassService;

  @Autowired
  private EmulatorService emulatorService;

  @BeforeAll
  public void setup() {
    setupSystem();
  }

  @Test
  public void testGetDirectB2SData() {
    Game game = gameService.getGameByBaseFilename(1, "Twister (1996)");
    assertNotNull(game);

    DirectB2SData data = backglassService.getDirectB2SData(game);
    assertNotNull(data);
    assertEquals(3, data.getTableType());
    assertNotNull(data.getAuthor());
  }

  @Test
  public void testGetDirectB2SDataByFilename() {
    DirectB2SData data = backglassService.getDirectB2SData(1, "Twister (1996).directb2s");
    assertNotNull(data);
    assertNotNull(data.getAuthor());

    DirectB2SData data2 = backglassService.getDirectB2SData(1, "Jaws.directb2s");
    assertNotNull(data2);
  }

  @Test
  public void testGetBackglasses() {
    List<DirectB2S> backglasses = backglassService.getBackglasses();
    assertNotNull(backglasses);
    assertFalse(backglasses.isEmpty());
    assertEquals(5, backglasses.size());
  }

  @Test
  public void testGetTableSettings() {
    Game game = gameService.getGameByBaseFilename(1, "Twister (1996)");
    assertNotNull(game);

    DirectB2STableSettings settings = backglassService.getTableSettings(game);
    assertNotNull(settings);
    assertEquals(2, settings.getHideGrill());
  }

  @Test
  public void testGetServerSettings() {
    DirectB2ServerSettings settings = backglassService.getServerSettings();
    assertNotNull(settings);
  }

  @Test
  public void testGetDirectB2SAndVersions() {
    Game game = gameService.getGameByBaseFilename(1, "Twister (1996)");
    assertNotNull(game);

    DirectB2S b2s = backglassService.getDirectB2SAndVersions(game);
    assertNotNull(b2s);
    assertEquals("Twister (1996).directb2s", b2s.getFileName());
    assertEquals(2, b2s.getNbVersions());
  }

  @Test
  public void testGetCacheDirectB2SAndVersions() {
    Game game = gameService.getGameByBaseFilename(1, "Twister (1996)");
    assertNotNull(game);

    DirectB2S cached = backglassService.getCacheDirectB2SAndVersions(game);
    // cached version may be null if not previously loaded
    if (cached != null) {
      assertNotNull(cached.getFileName());
    }
  }

  @Test
  public void testClearCache() {
    boolean result = backglassService.clearCache();
    assertTrue(result);
  }
}
