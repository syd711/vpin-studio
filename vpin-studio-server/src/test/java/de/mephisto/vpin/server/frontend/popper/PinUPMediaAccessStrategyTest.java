package de.mephisto.vpin.server.frontend.popper;

import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.frontend.popper.PopperSettings;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PinUPMediaAccessStrategyTest {

  private static final String MEDIA_DIR = "../testsystem/vPinball/PinUPSystem/POPMedia";
  private static final String GLOBAL_MEDIA_DIR = "../testsystem/vPinball/PinUPSystem/POPMedia/Default";

  private PinUPMediaAccessStrategy strategy;
  private PinUPConnector connector;

  @BeforeEach
  public void setUp() {
    connector = mock(PinUPConnector.class);
    PopperSettings settings = new PopperSettings();
    settings.setGlobalMediaDir(GLOBAL_MEDIA_DIR);
    when(connector.getSettings()).thenReturn(settings);

    strategy = new PinUPMediaAccessStrategy(connector);
  }

  private GameEmulator buildEmulator() {
    GameEmulator emulator = new GameEmulator();
    emulator.setType(EmulatorType.VisualPinball);
    emulator.setName("Visual Pinball X");
    emulator.setMediaDirectory(MEDIA_DIR + "/Visual Pinball X");
    emulator.setGamesDirectory("../testsystem/vPinball/VisualPinball/Tables/");
    return emulator;
  }

  private Game buildGame(String gameFileName, String gameName) {
    Game game = new Game();
    game.setGameFileName(gameFileName);
    game.setGameName(gameName);
    game.setEmulator(buildEmulator());
    game.setGameFile(new File("../testsystem/vPinball/VisualPinball/Tables/" + gameFileName));
    return game;
  }

  @Test
  public void testGetEmulatorMediaFolderByType() {
    File folder = strategy.getEmulatorMediaFolder(EmulatorType.VisualPinball);
    assertNotNull(folder);
    assertEquals("Visual Pinball X", folder.getName());
  }

  @Test
  public void testGetEmulatorMediaFolderByEmulatorAndScreen() {
    GameEmulator emulator = buildEmulator();
    File folder = strategy.getEmulatorMediaFolder(emulator, VPinScreen.Wheel);
    assertNotNull(folder);
    assertEquals("Wheel", folder.getName());
    assertTrue(folder.exists());
  }

  @Test
  public void testGetGameMediaFolder() {
    Game game = buildGame("Baseball (1970).vpx", "Baseball (1970)");
    File folder = strategy.getGameMediaFolder(game, VPinScreen.Wheel, null, false);
    assertNotNull(folder);
    assertEquals("Wheel", folder.getName());
    assertTrue(folder.exists());
  }

  @Test
  public void testGetGameMediaFolderForDifferentScreens() {
    Game game = buildGame("Baseball (1970).vpx", "Baseball (1970)");

    for (VPinScreen screen : new VPinScreen[]{VPinScreen.Wheel, VPinScreen.BackGlass, VPinScreen.PlayField, VPinScreen.Topper}) {
      File folder = strategy.getGameMediaFolder(game, screen, null, false);
      assertNotNull(folder);
      assertEquals(screen.name(), folder.getName());
    }
  }

  @Test
  public void testGetScreenMediaFiles() {
    Game game = buildGame("Baseball (1970).vpx", "Baseball (1970)");

    List<File> wheelFiles = strategy.getScreenMediaFiles(game, VPinScreen.Wheel, null);
    assertNotNull(wheelFiles);
    assertFalse(wheelFiles.isEmpty());
    assertTrue(wheelFiles.stream().allMatch(f -> f.getName().startsWith("Baseball (1970)")));
  }

  @Test
  public void testGetScreenMediaFilesForJaws() {
    Game game = buildGame("Jaws.vpx", "Jaws");

    List<File> wheelFiles = strategy.getScreenMediaFiles(game, VPinScreen.Wheel, null);
    assertNotNull(wheelFiles);
    assertFalse(wheelFiles.isEmpty());

    List<File> playFieldFiles = strategy.getScreenMediaFiles(game, VPinScreen.PlayField, null);
    assertNotNull(playFieldFiles);
    assertFalse(playFieldFiles.isEmpty());
  }

  @Test
  public void testGetScreenMediaFilesEmpty() {
    Game game = buildGame("NonExistent.vpx", "NonExistent");
    List<File> files = strategy.getScreenMediaFiles(game, VPinScreen.Wheel, null);
    assertNotNull(files);
    assertTrue(files.isEmpty());
  }

  @Test
  public void testGetPlaylistMediaFolder() {
    de.mephisto.vpin.server.playlists.Playlist playlist = mock(de.mephisto.vpin.server.playlists.Playlist.class);
    File folder = strategy.getPlaylistMediaFolder(playlist, VPinScreen.Wheel, false);
    assertNotNull(folder);
    assertEquals(VPinScreen.Wheel.getSegment(), folder.getName());
  }

  @Test
  public void testCreateMedia() {
    Game game = buildGame("Baseball (1970).vpx", "Baseball (1970)");
    File mediaFile = strategy.createMedia(game, VPinScreen.Wheel, "png", false);
    assertNotNull(mediaFile);
    assertEquals("Baseball (1970).png", mediaFile.getName());
  }
}