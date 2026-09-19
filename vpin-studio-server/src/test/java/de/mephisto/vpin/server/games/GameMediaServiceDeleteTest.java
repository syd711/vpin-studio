package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.descriptors.DeleteDescriptor;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.assets.AssetRepository;
import de.mephisto.vpin.server.assets.AssetService;
import de.mephisto.vpin.server.directb2s.BackglassService;
import de.mephisto.vpin.server.dmd.DMDService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.music.MusicService;
import de.mephisto.vpin.server.pinvol.PinVolService;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.system.DefaultPictureService;
import de.mephisto.vpin.server.vpinmame.VPinMameRomAliasService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.vpinmame.VPinMameService;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Deletes tables against a real folder layout. The per table layout (Linux standalone without a shared VPinMAME folder)
 * keeps the ROM, NVRAM and config next to the table, e.g. Tables/Twister/pinmame/roms/twister.zip.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GameMediaServiceDeleteTest {

  private static final String ROM = "twister";

  @Mock
  private EmulatorService emulatorService;
  @Mock
  private GameService gameService;
  @Mock
  private FrontendService frontendService;
  @Mock
  private HighscoreService highscoreService;
  @Mock
  private DefaultPictureService defaultPictureService;
  @Mock
  private PupPacksService pupPacksService;
  @Mock
  private PinVolService pinVolService;
  @Mock
  private DMDService dmdService;
  @Mock
  private VPinMameRomAliasService vPinMameRomAliasService;
  @Mock
  private AltSoundService altSoundService;
  @Mock
  private AltColorService altColorService;
  @Mock
  private BackglassService backglassService;
  @Mock
  private MusicService musicService;
  @Mock
  private AssetService assetService;
  @Mock
  private AssetRepository assetRepository;
  @Mock
  private GameDetailsRepositoryService gameDetailsRepositoryService;
  @Mock
  private GameLifecycleService gameLifecycleService;

  @Spy
  private VPinMameService vPinMameService = new VPinMameService();

  @InjectMocks
  private GameMediaService service;

  @TempDir
  Path tempDir;

  private File gamesFolder;
  private File tableFolder;
  private GameEmulator emulator;
  private Game game;

  @BeforeEach
  void setUp() throws IOException {
    gamesFolder = tempDir.resolve("Tables").toFile();
    tableFolder = new File(gamesFolder, "Twister");
    assertThat(tableFolder.mkdirs()).isTrue();

    FolderLookupService folderLookupService = new FolderLookupService();
    ReflectionTestUtils.setField(folderLookupService, "vPinMameService", vPinMameService);
    ReflectionTestUtils.setField(vPinMameService, "folderLookupService", folderLookupService);

    // the registry and the DMD device ini are Windows only
    doReturn(true).when(vPinMameService).deleteOptions(any());
    doReturn(true).when(vPinMameService).deleteDMDDeviceIniEntry(any());

    // the collaborators that are not about files in the table folder report success
    when(defaultPictureService.deleteAllPictures(any())).thenReturn(true);
    when(pupPacksService.delete(any())).thenReturn(true);
    when(altSoundService.delete(any())).thenReturn(true);
    when(altColorService.delete(any())).thenReturn(true);
    when(musicService.delete(any())).thenReturn(true);
    when(backglassService.deleteB2STableSettings(any())).thenReturn(true);
    when(vPinMameRomAliasService.deleteAlias(any(), any())).thenReturn(true);
    when(frontendService.deleteGame(anyInt())).thenReturn(true);

    emulator = new GameEmulator();
    emulator.setId(1);
    emulator.setType(EmulatorType.VisualPinball);
    emulator.setGameExt("vpx");
    emulator.setInstallationDirectory(tempDir.toFile().getAbsolutePath());
    emulator.setGamesDirectory(gamesFolder.getAbsolutePath());
    emulator.setPerTableFileStructure(true);

    game = createGame(1, tableFolder, "Twister.vpx");
    when(gameService.getGame(1)).thenReturn(game);
    when(emulatorService.getGameEmulator(1)).thenReturn(emulator);
  }

  // ---- ROM ----

  @Test
  void deleteRom_perTableLayout_deletesRomNextToTable() throws IOException {
    File romZip = touch(tableFolder, "pinmame/roms/" + ROM + ".zip");
    File nvram = touch(tableFolder, "pinmame/nvram/" + ROM + ".nv");

    DeleteDescriptor descriptor = tableFilesOnly();
    descriptor.setDeleteRom(true);

    assertThat(service.deleteGame(descriptor)).isTrue();

    assertThat(romZip).doesNotExist();
    assertThat(new File(tableFolder, "Twister.vpx")).doesNotExist();
    assertThat(nvram).exists();
    assertThat(tableFolder).exists();
  }

  @Test
  void deleteRom_perTableLayout_keepsRomWhenNotSelected() throws IOException {
    File romZip = touch(tableFolder, "pinmame/roms/" + ROM + ".zip");

    assertThat(service.deleteGame(tableFilesOnly())).isTrue();

    assertThat(romZip).exists();
    assertThat(tableFolder).exists();
  }

  @Test
  void deleteRom_perTableLayout_findsRomWithDifferentCase() throws IOException {
    File romZip = touch(tableFolder, "pinmame/roms/TWISTER.zip");

    DeleteDescriptor descriptor = tableFilesOnly();
    descriptor.setDeleteRom(true);
    service.deleteGame(descriptor);

    assertThat(romZip).doesNotExist();
  }

  // ---- table folder ----

  @Test
  void deleteEverything_perTableLayout_deletesTableFolder() throws IOException {
    touch(tableFolder, "pinmame/roms/" + ROM + ".zip");
    touch(tableFolder, "pinmame/nvram/" + ROM + ".nv");
    touch(tableFolder, "pinmame/cfg/" + ROM + ".cfg");
    touch(tableFolder, "user/VPReg.stg");
    touch(tableFolder, "music/theme.mp3");
    touch(tableFolder, "altsound/" + ROM + "/altsound.csv");
    touch(tableFolder, "Twister.directb2s");
    touch(tableFolder, "Twister.ini");

    assertThat(service.deleteGame(everything())).isTrue();

    assertThat(tableFolder).doesNotExist();
    assertThat(gamesFolder).exists();
  }

  @Test
  void deleteEverything_perTableLayout_keepsFolderWithAnotherTable() throws IOException {
    File other = touch(tableFolder, "Twister Remake.vpx");
    File romZip = touch(tableFolder, "pinmame/roms/" + ROM + ".zip");

    service.deleteGame(everything());

    assertThat(other).exists();
    assertThat(tableFolder).exists();
    assertThat(new File(tableFolder, "Twister.vpx")).doesNotExist();
    assertThat(romZip).doesNotExist();
  }

  @Test
  void deleteEverything_perTableLayout_neverDeletesGamesFolder() throws IOException {
    File looseTable = touch(gamesFolder, "Loose.vpx");
    Game loose = createGame(2, gamesFolder, "Loose.vpx");
    when(gameService.getGame(2)).thenReturn(loose);
    touch(gamesFolder, "Twister/Twister.vpx");

    DeleteDescriptor descriptor = everything();
    descriptor.setGameIds(List.of(2));
    service.deleteGame(descriptor);

    assertThat(looseTable).doesNotExist();
    assertThat(gamesFolder).exists();
    assertThat(tableFolder).exists();
  }

  @Test
  void deleteEverything_batchInSameFolder_deletesFolderOnce() throws IOException {
    touch(tableFolder, "Twister Remake.vpx");
    Game remake = createGame(2, tableFolder, "Twister Remake.vpx");
    when(gameService.getGame(2)).thenReturn(remake);

    DeleteDescriptor descriptor = everything();
    descriptor.setGameIds(List.of(1, 2));

    assertThat(service.deleteGame(descriptor)).isTrue();

    assertThat(tableFolder).doesNotExist();
  }

  @Test
  void deleteSelection_perTableLayout_keepsFolderWithRemainingFiles() throws IOException {
    File nvram = touch(tableFolder, "pinmame/nvram/" + ROM + ".nv");

    service.deleteGame(tableFilesOnly());

    assertThat(nvram).exists();
    assertThat(tableFolder).exists();
  }

  @Test
  void deleteSelection_perTableLayout_deletesEmptyFolder() {
    assertThat(service.deleteGame(tableFilesOnly())).isTrue();

    assertThat(tableFolder).doesNotExist();
  }

  // ---- failing steps ----

  @Test
  void failingStep_doesNotSkipTheFollowingSteps() throws IOException {
    File romZip = touch(tableFolder, "pinmame/roms/" + ROM + ".zip");
    doThrow(new IllegalStateException("highscores")).when(highscoreService).deleteHighscore(any());
    when(pupPacksService.delete(any())).thenThrow(new IllegalStateException("pup pack"));
    when(vPinMameRomAliasService.deleteAlias(any(), any())).thenThrow(new IllegalStateException("alias"));
    doThrow(new IllegalStateException("dmd device ini")).when(vPinMameService).deleteDMDDeviceIniEntry(any());

    assertThat(service.deleteGame(everything())).isFalse();

    assertThat(romZip).doesNotExist();
    assertThat(new File(tableFolder, "Twister.vpx")).doesNotExist();
    assertThat(tableFolder).doesNotExist();
    verify(frontendService).deleteGame(1);
    verify(gameLifecycleService).notifyGameDeleted(1);
  }

  @Test
  void failingGame_doesNotSkipTheFollowingGame() throws IOException {
    File otherFolder = new File(gamesFolder, "Other");
    Game other = createGame(2, otherFolder, "Other.vpx");
    File otherRom = touch(otherFolder, "pinmame/roms/" + ROM + ".zip");
    when(gameService.getGame(1)).thenThrow(new IllegalStateException("broken game"));
    when(gameService.getGame(2)).thenReturn(other);

    DeleteDescriptor descriptor = everything();
    descriptor.setGameIds(List.of(1, 2));

    assertThat(service.deleteGame(descriptor)).isFalse();

    assertThat(otherRom).doesNotExist();
    assertThat(otherFolder).doesNotExist();
  }

  @Test
  void unknownGame_doesNotSkipTheFollowingGame() throws IOException {
    when(gameService.getGame(3)).thenReturn(null);

    DeleteDescriptor descriptor = tableFilesOnly();
    descriptor.setGameIds(List.of(3, 1));

    assertThat(service.deleteGame(descriptor)).isFalse();

    assertThat(new File(tableFolder, "Twister.vpx")).doesNotExist();
  }

  @Test
  void failingMediaAsset_doesNotSkipTheOtherAssets() throws IOException {
    File first = touch(tempDir.toFile(), "media/first.png");
    File second = touch(tempDir.toFile(), "media/second.png");
    FrontendMediaItem brokenItem = mock(FrontendMediaItem.class);
    when(brokenItem.getFile()).thenThrow(new IllegalStateException("broken item"));
    FrontendMediaItem firstItem = mock(FrontendMediaItem.class);
    when(firstItem.getFile()).thenReturn(first);
    FrontendMediaItem secondItem = mock(FrontendMediaItem.class);
    when(secondItem.getFile()).thenReturn(second);
    when(frontendService.getMediaItems(any(), eq(VPinScreen.Wheel))).thenReturn(List.of(firstItem));
    when(frontendService.getMediaItems(any(), eq(VPinScreen.BackGlass))).thenReturn(List.of(secondItem));
    when(frontendService.getMediaItems(any(), eq(VPinScreen.Topper))).thenReturn(List.of(brokenItem));

    assertThat(service.deleteGame(everything())).isFalse();

    assertThat(first).doesNotExist();
    assertThat(second).doesNotExist();
  }

  // ---- legacy layout ----

  @Test
  void deleteEverything_legacyLayout_keepsTableFolder() throws IOException {
    emulator.setPerTableFileStructure(false);
    emulator.setMameDirectory(tempDir.resolve("VPinMAME").toFile().getAbsolutePath());
    File sharedRom = touch(tempDir.resolve("VPinMAME").toFile(), "roms/" + ROM + ".zip");
    doReturn(sharedRom.getParentFile()).when(vPinMameService).getRomsFolder();
    doReturn(new File(sharedRom.getParentFile().getParentFile(), "cfg")).when(vPinMameService).getCfgFolder();
    File extra = touch(tableFolder, "notes.txt");

    service.deleteGame(everything());

    assertThat(sharedRom).doesNotExist();
    assertThat(extra).exists();
    assertThat(tableFolder).exists();
  }

  // ---- helpers ----

  private Game createGame(int id, File folder, String fileName) throws IOException {
    File gameFile = touch(folder, fileName);
    Game g = new Game();
    g.setId(id);
    g.setEmulator(emulator);
    g.setGameFile(gameFile);
    g.setGameFileName(fileName);
    g.setRom(ROM);
    g.setScannedRom(ROM);
    return g;
  }

  private static File touch(File folder, String relativePath) throws IOException {
    File file = new File(folder, relativePath);
    Files.createDirectories(file.getParentFile().toPath());
    Files.writeString(file.toPath(), "x");
    return file;
  }

  private static DeleteDescriptor tableFilesOnly() {
    DeleteDescriptor descriptor = nothing();
    descriptor.setDeleteTable(true);
    return descriptor;
  }

  private static DeleteDescriptor nothing() {
    DeleteDescriptor d = new DeleteDescriptor();
    d.setDeleteTable(false);
    d.setDeleteDirectB2s(false);
    d.setDeleteFromFrontend(false);
    d.setDeletePupPack(false);
    d.setDeleteDMDs(false);
    d.setDeleteHighscores(false);
    d.setDeleteMusic(false);
    d.setDeleteAltSound(false);
    d.setDeleteAltColor(false);
    d.setDeleteCfg(false);
    d.setDeleteRom(false);
    d.setDeleteBAMCfg(false);
    d.setDeletePov(false);
    d.setDeleteRes(false);
    d.setDeleteIni(false);
    d.setDeleteVbs(false);
    d.setDeletePinVol(false);
    d.setDeleteAlias(false);
    d.setDeleteB2STableSettings(false);
    d.setDeleteDMDDeviceIni(false);
    d.setGameIds(List.of(1));
    return d;
  }

  private static DeleteDescriptor everything() {
    DeleteDescriptor d = nothing();
    d.setDeleteTable(true);
    d.setDeleteDirectB2s(true);
    d.setDeleteFromFrontend(true);
    d.setDeletePupPack(true);
    d.setDeleteDMDs(true);
    d.setDeleteHighscores(true);
    d.setDeleteMusic(true);
    d.setDeleteAltSound(true);
    d.setDeleteAltColor(true);
    d.setDeleteCfg(true);
    d.setDeleteRom(true);
    d.setDeleteBAMCfg(true);
    d.setDeletePov(true);
    d.setDeleteRes(true);
    d.setDeleteIni(true);
    d.setDeleteVbs(true);
    d.setDeletePinVol(true);
    d.setDeleteAlias(true);
    d.setDeleteB2STableSettings(true);
    d.setDeleteDMDDeviceIni(true);
    return d;
  }
}
