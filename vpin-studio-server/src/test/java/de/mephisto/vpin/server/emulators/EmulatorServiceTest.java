package de.mephisto.vpin.server.emulators;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.games.GameEmulator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class EmulatorServiceTest extends AbstractVPinServerTest {

  @Autowired
  private EmulatorService emulatorService;

  @BeforeAll
  public void setup() {
    setupSystem();
  }

  @Test
  public void testGetGameEmulators() {
    Collection<GameEmulator> emulators = emulatorService.getGameEmulators();
    assertNotNull(emulators);
    assertFalse(emulators.isEmpty());
  }

  @Test
  public void testGetGameEmulator() {
    GameEmulator emulator = emulatorService.getGameEmulator(1);
    assertNotNull(emulator);
    assertNotNull(emulator.getName());
    assertNotNull(emulator.getGamesDirectory());
  }

  @Test
  public void testGetValidGameEmulators() {
    List<GameEmulator> valid = emulatorService.getValidGameEmulators();
    assertNotNull(valid);
    assertFalse(valid.isEmpty());
  }

  @Test
  public void testGetValidatedGameEmulators() {
    List<GameEmulator> validated = emulatorService.getValidatedGameEmulators();
    assertNotNull(validated);
  }

  @Test
  public void testGetVpxGameEmulators() {
    List<GameEmulator> vpx = emulatorService.getVpxGameEmulators();
    assertNotNull(vpx);
  }

  @Test
  public void testGetBackglassGameEmulators() {
    List<GameEmulator> backglass = emulatorService.getBackglassGameEmulators();
    assertNotNull(backglass);
  }

  @Test
  public void testGetAltExeNames() {
    List<String> altExeNames = emulatorService.getAltExeNames(1);
    assertNotNull(altExeNames);
  }

  @Test
  public void testClearCache() {
    boolean result = emulatorService.clearCache();
    assertTrue(result);
  }
}
