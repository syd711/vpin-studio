package de.mephisto.vpin.server.vr;

import de.mephisto.vpin.restclient.preferences.VRSettings;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.TableStatusChangedEvent;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VRServiceTableIniTest {

  @TempDir
  Path tempDir;

  @Mock
  private PreferencesService preferencesService;

  @Mock
  private EmulatorService emulatorService;

  @Mock
  private FrontendStatusService frontendStatusService;

  @InjectMocks
  private VRService vrService;

  private VRSettings vrSettings;
  private VRTableIniSwapper swapper;

  @BeforeEach
  void setUp() {
    vrSettings = new VRSettings();
    swapper = new VRTableIniSwapper(tempDir.resolve("journal.txt").toFile());
    ReflectionTestUtils.setField(vrService, "vrSettings", vrSettings);
    ReflectionTestUtils.setField(vrService, "tableIniSwapper", swapper);
  }

  @Test
  void toggleVRMode_featureDisabled_doesNothing() {
    vrSettings.setEnabled(false);

    assertFalse(vrService.toggleVRMode());

    assertFalse(vrSettings.isVrEnabled());
    verifyNoInteractions(preferencesService, emulatorService);
  }

  @Test
  void isVRModeActive_requiresFeatureAndToggle() {
    vrSettings.setEnabled(false);
    vrSettings.setVrEnabled(true);
    assertFalse(vrService.isVRModeActive());

    vrSettings.setEnabled(true);
    vrSettings.setVrEnabled(false);
    assertFalse(vrService.isVRModeActive());

    vrSettings.setVrEnabled(true);
    assertTrue(vrService.isVRModeActive());
  }

  @Test
  void tableLaunching_vrModeInactive_keepsDesktopIni() throws IOException {
    vrSettings.setEnabled(true);
    vrSettings.setVrEnabled(false);
    Game game = vpxGame("Table");
    write(game.getIniFile(), "desktop");
    write(game.getVRIniFile(), "vr");

    vrService.tableLaunching(game);

    assertEquals("desktop", read(game.getIniFile()));
    assertEquals("vr", read(game.getVRIniFile()));
  }

  @Test
  void tableLaunching_featureDisabled_keepsDesktopIni() throws IOException {
    vrSettings.setEnabled(false);
    vrSettings.setVrEnabled(true);
    Game game = vpxGame("Table");
    write(game.getIniFile(), "desktop");
    write(game.getVRIniFile(), "vr");

    vrService.tableLaunching(game);

    assertEquals("desktop", read(game.getIniFile()));
  }

  @Test
  void tableLaunching_nonVpxGame_keepsDesktopIni() throws IOException {
    activateVRMode();
    Game game = vpxGame("Table");
    when(game.isVpxGame()).thenReturn(false);
    write(game.getIniFile(), "desktop");
    write(game.getVRIniFile(), "vr");

    vrService.tableLaunching(game);

    assertEquals("desktop", read(game.getIniFile()));
  }

  @Test
  void launchAndExit_vrModeActive_swapsAndRestores() throws IOException {
    activateVRMode();
    Game game = vpxGame("Table");
    write(game.getIniFile(), "desktop");
    write(game.getVRIniFile(), "vr");

    vrService.tableLaunching(game);
    assertEquals("vr", read(game.getIniFile()));
    assertFalse(game.getVRIniFile().exists());

    vrService.tableExited(event(game));
    assertEquals("desktop", read(game.getIniFile()));
    assertEquals("vr", read(game.getVRIniFile()));
  }

  @Test
  void tableExited_restoresEvenIfVrModeWasLeft() throws IOException {
    activateVRMode();
    Game game = vpxGame("Table");
    write(game.getIniFile(), "desktop");
    write(game.getVRIniFile(), "vr");
    vrService.tableLaunching(game);

    vrSettings.setVrEnabled(false);
    vrService.tableExited(event(game));

    assertEquals("desktop", read(game.getIniFile()));
    assertEquals("vr", read(game.getVRIniFile()));
  }

  @Test
  void tableLaunching_restoresSwapOfOtherTable() throws IOException {
    activateVRMode();
    Game first = vpxGame("First");
    Game second = vpxGame("Second");
    write(first.getIniFile(), "first desktop");
    write(first.getVRIniFile(), "first vr");
    write(second.getIniFile(), "second desktop");
    write(second.getVRIniFile(), "second vr");

    //launched from the Studio, no exit event received
    vrService.tableLaunching(first);
    vrService.tableLaunching(second);

    assertEquals("first desktop", read(first.getIniFile()));
    assertEquals("first vr", read(first.getVRIniFile()));
    assertEquals("second vr", read(second.getIniFile()));
  }

  @Test
  void createTableVRIni_copiesDesktopIni() throws IOException {
    Game game = vpxGame("Table");
    write(game.getIniFile(), "desktop");

    assertTrue(vrService.createTableVRIni(game));

    assertEquals("desktop", read(game.getVRIniFile()));
  }

  @Test
  void createTableVRIni_withoutDesktopIni_createsEmptyFile() throws IOException {
    Game game = vpxGame("Table");

    assertTrue(vrService.createTableVRIni(game));

    assertEquals("", read(game.getVRIniFile()));
  }

  @Test
  void createAndDeleteTableVRIni_refusedWhileSwapped() throws IOException {
    activateVRMode();
    Game game = vpxGame("Table");
    write(game.getIniFile(), "desktop");
    write(game.getVRIniFile(), "vr");
    vrService.tableLaunching(game);

    assertFalse(vrService.createTableVRIni(game));
    assertFalse(vrService.deleteTableVRIni(game));
    assertEquals("vr", read(game.getIniFile()));
  }

  @Test
  void deleteTableVRIni_deletesFile() throws IOException {
    Game game = vpxGame("Table");
    write(game.getVRIniFile(), "vr");

    assertTrue(vrService.deleteTableVRIni(game));

    assertFalse(game.getVRIniFile().exists());
  }

  private void activateVRMode() {
    vrSettings.setEnabled(true);
    vrSettings.setVrEnabled(true);
  }

  private Game vpxGame(String name) {
    File iniFile = tempDir.resolve(name + ".ini").toFile();
    Game game = mock(Game.class);
    lenient().when(game.isVpxGame()).thenReturn(true);
    lenient().when(game.getIniFile()).thenReturn(iniFile);
    lenient().when(game.getVRIniFile()).thenReturn(VRTableIniSwapper.getVRIniFile(iniFile));
    lenient().when(game.getGameDisplayName()).thenReturn(name);
    return game;
  }

  private static TableStatusChangedEvent event(Game game) {
    TableStatusChangedEvent event = mock(TableStatusChangedEvent.class);
    when(event.getGame()).thenReturn(game);
    return event;
  }

  private static void write(File file, String content) throws IOException {
    Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
  }

  private static String read(File file) throws IOException {
    return Files.readString(file.toPath(), StandardCharsets.UTF_8);
  }
}
