package de.mephisto.vpin.server.frontend;

import static org.junit.Assert.*;

import java.util.List;

import de.mephisto.vpin.server.games.GameEmulator;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.frontend.pinballx.PinballXConnector;
import de.mephisto.vpin.server.frontend.pinbally.PinballYConnector;
import de.mephisto.vpin.server.frontend.popper.PinUPConnector;
import de.mephisto.vpin.server.frontend.standalone.StandaloneConnector;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.preferences.PreferencesService;

@SpringBootTest
public class FrontendConnectorsTest extends AbstractVPinServerTest {

  @Autowired
  private PreferencesService preferencesService;

  /** temporary saved */
  private FrontendType saved;

  @BeforeAll
  public void init() throws Exception {
    saved = systemService.getFrontendType();

    // needed as popper test database is not 1.5 compatible
    ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    serverSettings.setMappingVpsTableId("CUSTOM2");
    serverSettings.setMappingVpsTableVersionId("CUSTOM3");
    preferencesService.savePreference(serverSettings);
  }
  @AfterAll
  public void done() throws Exception {
    systemService.setFrontendType(saved);
    frontendService.afterPropertiesSet();
  }

  @Test
  public void testPinUPConnector() {
    setupSystem(FrontendType.Popper);
    assertTrue(frontendService.getFrontendConnector() instanceof PinUPConnector);

    int id = testLoad(5, 3);   
    testSave(id); 
    testVpsLink(id);
  }

  @Test
  public void testPinballXConnector() {
    setupSystem(FrontendType.PinballX);
    assertTrue(frontendService.getFrontendConnector() instanceof PinballXConnector);

    int id = testLoad(8, 3);
    testSave(id); 
  }

  @Test
  public void testPinballYConnector() {
    setupSystem(FrontendType.PinballY);
    assertTrue(frontendService.getFrontendConnector() instanceof PinballYConnector);

    int id = testLoad(7, 3);
    testSave(id);
  }

  @Test
  public void testStandaloneConnector() {
    setupSystem(FrontendType.Standalone);
    assertTrue(frontendService.getFrontendConnector() instanceof StandaloneConnector);

    testLoad(1, 3);
  }

  //------------------------------------------------

  public int testLoad(int expectedNbEmulators, int expectedNbGames) {
    FrontendConnector connector = frontendService.getFrontendConnector();
    // check installation folder setup
    assertTrue(connector.getInstallationFolder().exists());
    // check emulators have been loaded
    List<GameEmulator> emulators = connector.getEmulators();
    assertEquals(expectedNbEmulators, emulators.size());

    // first one should be visual pinball and with id=1 else other tests will fail
    GameEmulator vpx = emulators.get(0);
    assertTrue(vpx.getType().isVpxEmulator());
    assertTrue(vpx.isEnabled());
    assertEquals(1, vpx.getId());
    assertTrue(StringUtils.startsWithIgnoreCase(vpx.getName().replace(" ", ""), "VisualPinball"));

    int gameCount = connector.getGameCount(1);
    assertEquals(expectedNbGames, gameCount);

    List<Integer> gameIds = connector.getGameIds(1);
    for (int id: gameIds) {
      Game g = connector.getGame(id);
      assertEquals(id, g.getId());
      System.out.println(g.getGameName() + " - " + g.getId());

      TableDetails td = connector.getTableDetails(id);
      // protect as Standalone returns null
      if (td != null) {
        assertEquals(g.getGameName(), td.getGameName());
        assertEquals(g.getGameDisplayName(), td.getGameDisplayName());
        assertEquals(g.getGameFileName(), td.getGameFileName());
      }
    }

    Game twister = connector.getGameByFilename(vpx.getId(), NVRAM_TABLE_NAME);
    assertNotNull(twister);
    assertEquals("Twister (1996)", twister.getGameName());

    Game notfound = connector.getGameByFilename(vpx.getId(), "notfound.vpx");
    assertNull(notfound);

    return twister.getId();
  }

  public void testSave(int id) {
    FrontendConnector connector = frontendService.getFrontendConnector();
    TableDetails td = connector.getTableDetails(id);
    String author = StringUtils.defaultString(td.getAuthor());
    assertEquals("", author);

    td.setAuthor("Test Osterone");
    connector.saveTableDetails(id, td);

    TableDetails updated = connector.getTableDetails(id);
    assertEquals("Test Osterone", updated.getAuthor());
  }

  public void testVpsLink(int id) {
    FrontendConnector connector = frontendService.getFrontendConnector();

    TableDetails td = connector.getTableDetails(id);
    String vpsTableId = td.getCustom2();
    String vpsVersionId = td.getCustom3();
    assertNull(vpsTableId);
    assertNull(vpsVersionId);

    connector.vpsLink(id, "123456789", "987654321");

    TableDetails updated = connector.getTableDetails(id);
    assertEquals("123456789", updated.getCustom2());
    assertEquals("987654321", updated.getCustom3());
  }
}