package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.backups.adapters.vpa.VpaService;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.dmd.DMDService;
import de.mephisto.vpin.server.doflinx.DOFLinxService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.fp.FuturePinballService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.highscores.HighscoreBackupService;
import de.mephisto.vpin.server.mame.MameService;
import de.mephisto.vpin.server.music.MusicService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.vps.VpsService;
import de.mephisto.vpin.server.vpinmame.VPinMameRomAliasService;
import de.mephisto.vpin.server.vpinmame.VPinMameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UniversalUploadServiceTest {

  @Mock private GameService gameService;
  @Mock private DMDService dmdService;
  @Mock private VPinMameService vPinMameService;
  @Mock private MameService mameService;
  @Mock private FuturePinballService futurePinballService;
  @Mock private AltColorService altColorService;
  @Mock private AltSoundService altSoundService;
  @Mock private MusicService musicService;
  @Mock private GameMediaService gameMediaService;
  @Mock private PupPacksService pupPacksService;
  @Mock private VpsService vpsService;
  @Mock private DiscordService discordService;
  @Mock private VPinMameRomAliasService VPinMameRomAliasService;
  @Mock private PreferencesService preferencesService;
  @Mock private EmulatorService emulatorService;
  @Mock private GameLifecycleService gameLifecycleService;
  @Mock private HighscoreBackupService highscoreBackupService;
  @Mock private DOFLinxService dofLinxService;
  @Mock private VpaService vpaService;
  @Mock private FrontendService frontendService;

  @InjectMocks
  private UniversalUploadService universalUploadService;

  @Test
  void create_returnsEmptyDescriptor() {
    UploadDescriptor descriptor = universalUploadService.create();

    assertThat(descriptor).isNotNull();
    assertThat(descriptor.getGameId()).isEqualTo(0);
  }

  @Test
  void error_setsErrorMessage() {
    UploadDescriptor descriptor = universalUploadService.error("something went wrong");

    assertThat(descriptor).isNotNull();
    assertThat(descriptor.getError()).isEqualTo("something went wrong");
  }

  @Test
  void create_withMultipartFile_setsGameIdToZero() {
    var multipart = mock(org.springframework.web.multipart.MultipartFile.class);
    when(multipart.getOriginalFilename()).thenReturn("table.vpx");

    UploadDescriptor descriptor = universalUploadService.create(multipart);

    assertThat(descriptor.getGameId()).isEqualTo(0);
    assertThat(descriptor.getOriginalUploadFileName()).isEqualTo("table.vpx");
  }

  @Test
  void create_withMultipartFileAndGameId_setsGameId() {
    var multipart = mock(org.springframework.web.multipart.MultipartFile.class);
    when(multipart.getOriginalFilename()).thenReturn("table.vpx");

    UploadDescriptor descriptor = universalUploadService.create(multipart, 42);

    assertThat(descriptor.getGameId()).isEqualTo(42);
  }

  @Test
  void resolveLinks_nonLinkFilename_doesNothing() throws Exception {
    UploadDescriptor descriptor = new UploadDescriptor();
    descriptor.setOriginalUploadFileName("regular_file.zip");
    descriptor.setTempFilename("/tmp/regular_file.zip");

    // No exception and vpsService not called
    universalUploadService.resolveLinks(descriptor);

    verifyNoInteractions(vpsService);
    assertThat(descriptor.getOriginalUploadFileName()).isEqualTo("regular_file.zip");
  }

  @Test
  void importFileBasedAssets_vrIniFile_isCopiedToVrIni(@TempDir Path tempDir) throws Exception {
    Game game = game(tempDir);
    Files.writeString(game.getIniFile().toPath(), "desktop");
    File upload = tempDir.resolve("upload.tmp").toFile();
    Files.writeString(upload.toPath(), "vr");

    UploadDescriptor descriptor = new UploadDescriptor();
    descriptor.setGameId(1);
    descriptor.setOriginalUploadFileName("Table.vr.ini");
    descriptor.setTempFilename(upload.getAbsolutePath());

    universalUploadService.importFileBasedAssets(descriptor, AssetType.INI);

    assertThat(Files.readString(game.getIniFile().toPath())).isEqualTo("desktop");
    assertThat(Files.readString(game.getVRIniFile().toPath())).isEqualTo("vr");
  }

  @Test
  void importFileBasedAssets_archiveWithIniAndVrIni_restoresBoth(@TempDir Path tempDir) throws Exception {
    Game game = game(tempDir);
    File archive = tempDir.resolve("backup.zip").toFile();
    try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(archive))) {
      zip.putNextEntry(new ZipEntry("Table.vr.ini"));
      zip.write("vr".getBytes(StandardCharsets.UTF_8));
      zip.closeEntry();
      zip.putNextEntry(new ZipEntry("Table.ini"));
      zip.write("desktop".getBytes(StandardCharsets.UTF_8));
      zip.closeEntry();
    }

    UploadDescriptor descriptor = new UploadDescriptor();
    descriptor.setGameId(1);
    descriptor.setOriginalUploadFileName("backup.zip");
    descriptor.setTempFilename(archive.getAbsolutePath());

    universalUploadService.importFileBasedAssets(descriptor, AssetType.INI);

    assertThat(Files.readString(game.getIniFile().toPath())).isEqualTo("desktop");
    assertThat(Files.readString(game.getVRIniFile().toPath())).isEqualTo("vr");
  }

  private Game game(Path tempDir) throws IOException {
    File gameFile = tempDir.resolve("Table.vpx").toFile();
    Files.writeString(gameFile.toPath(), "");
    Game game = new Game();
    game.setGameFile(gameFile);
    game.setGameFileName(gameFile.getName());
    when(gameService.getGame(1)).thenReturn(game);
    return game;
  }
}
