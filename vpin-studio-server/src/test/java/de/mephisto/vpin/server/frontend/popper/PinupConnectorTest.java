package de.mephisto.vpin.server.frontend.popper;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.Emulator;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.preferences.PreferencesService;

@SpringBootTest
public class PinupConnectorTest extends AbstractVPinServerTest {

  @BeforeAll
  public void init() throws Exception {
    // needed as popper test database is not 1.5 compatible
    
    ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    serverSettings.setMappingVpsTableId("CUSTOM2");
    serverSettings.setMappingVpsTableVersionId("CUSTOM3");
    preferencesService.savePreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
  
    setupSystem(FrontendType.Popper);
  
  }

  @Autowired
  private PreferencesService preferencesService;

  @Test
  public void testLoad() {

    FrontendConnector connector = frontendService.getFrontendConnector();
    assertTrue(connector instanceof PinUPConnector);

    List<Emulator> emulators = connector.getEmulators();
    assertEquals(5, emulators.size());
    Emulator vpx = emulators.get(0);
    assertTrue(vpx.isVisualPinball());
    assertTrue(vpx.isEnabled());
    assertEquals(1, vpx.getId());
    assertEquals("Visual Pinball X", vpx.getName());

    int gameCount = connector.getGameCount(1);
    assertEquals(3,gameCount);

    List<Integer> gameIds = connector.getGameIds(1);
    for (int id: gameIds) {
      Game g = connector.getGame(id);
      assertEquals(id, g.getId());
      //System.out.println(g.getGameName() + " - " + g.getId());

      TableDetails td = connector.getTableDetails(id);
      assertEquals(g.getGameName(), td.getGameName());
      assertEquals(g.getGameDisplayName(), td.getGameDisplayName());
      assertEquals(g.getGameFileName(), td.getGameFileName());
    }

    Game twister = connector.getGameByFilename(NVRAM_TABLE_NAME);
    assertNotNull(twister);
    assertEquals("Twister (1996)", twister.getGameName());

    Game notfound = connector.getGameByFilename("notfound.vpx");
    assertNull(notfound);
  }

  @Test
  public void testSave() {
    FrontendConnector connector = frontendService.getFrontendConnector();
    // get Twister
    int id = 3;
    TableDetails td = connector.getTableDetails(id);
    String author = td.getAuthor();
    assertEquals("", author);

    td.setAuthor("Test Osterone");
    connector.saveTableDetails(id, td);

    TableDetails updated = connector.getTableDetails(id);
    assertEquals("Test Osterone", updated.getAuthor());
  }

  @Test
  public void testVpsLink() {
    FrontendConnector connector = frontendService.getFrontendConnector();
    // use JAWS
    int id = 2;

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