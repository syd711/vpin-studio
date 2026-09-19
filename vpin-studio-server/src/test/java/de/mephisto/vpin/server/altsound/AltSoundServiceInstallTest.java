package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.vpinmame.VPinMameOptions;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.vpinmame.VPinMameService;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AltSoundServiceInstallTest {

  @Mock
  private AltSoundBackupService altSoundBackupService;
  @Mock
  private VPinMameService vPinMameService;
  @Mock
  private EmulatorService emulatorService;
  @Mock
  private GameLifecycleService gameLifecycleService;
  @Mock
  private FolderLookupService folderLookupService;

  @InjectMocks
  private AltSoundService altSoundService;

  @TempDir
  Path tempDir;

  @Test
  void installsIntoTheFolderThatIsReadBack() throws Exception {
    Game game = mock(Game.class);
    when(game.getRomAlias()).thenReturn("twst_alias");
    File aliasFolder = tempDir.resolve("altsound/twst_alias").toFile();
    when(folderLookupService.getAltSoundFolder(game, "twst_alias")).thenReturn(aliasFolder);
    VPinMameOptions options = mock(VPinMameOptions.class);
    when(vPinMameService.getOptions("twst_405")).thenReturn(options);

    altSoundService.installAltSound(game, "twst_405", createArchive(), null);

    verify(folderLookupService, never()).getAltSoundFolder(game, "twst_405");
    // the VPinMAME options belong to the ROM, not to the alias
    verify(options).setSoundMode(1);
    verify(vPinMameService).saveOptions(any());
  }

  @Test
  void installsIntoTheRomFolderWithoutAlias() throws Exception {
    Game game = mock(Game.class);
    File romFolder = tempDir.resolve("altsound/twst_405").toFile();
    when(folderLookupService.getAltSoundFolder(game, "twst_405")).thenReturn(romFolder);
    when(vPinMameService.getOptions("twst_405")).thenReturn(mock(VPinMameOptions.class));

    altSoundService.installAltSound(game, "twst_405", createArchive(), null);

    verify(folderLookupService).getAltSoundFolder(game, "twst_405");
  }

  private File createArchive() throws Exception {
    File archive = tempDir.resolve("altsound.zip").toFile();
    try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(archive))) {
      out.putNextEntry(new ZipEntry("altsound.csv"));
      out.write("ID,CHANNEL".getBytes());
      out.closeEntry();
    }
    return archive;
  }
}
