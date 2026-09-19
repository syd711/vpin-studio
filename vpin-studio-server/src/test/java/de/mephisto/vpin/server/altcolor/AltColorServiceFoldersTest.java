package de.mephisto.vpin.server.altcolor;

import de.mephisto.vpin.restclient.altcolor.AltColor;
import de.mephisto.vpin.server.doflinx.DOFLinxService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.vpinmame.VPinMameService;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * VPX 10.8.1 keeps Serum files in serum/&lt;rom&gt; and VNI/PAL files in vni/&lt;rom&gt; next to the table.
 */
@ExtendWith(MockitoExtension.class)
public class AltColorServiceFoldersTest {

  @Mock
  private VPinMameService vPinMameService;
  @Mock
  private DOFLinxService dofLinxService;
  @Mock
  private GameLifecycleService gameLifecycleService;
  @Mock
  private FolderLookupService folderLookupService;

  @InjectMocks
  private AltColorService altColorService;

  @TempDir
  Path tempDir;

  private File serumFolder;
  private File vniFolder;
  private Game game;

  @BeforeEach
  void setup() {
    serumFolder = tempDir.resolve("serum/twst_405").toFile();
    vniFolder = tempDir.resolve("vni/twst_405").toFile();

    game = mock(Game.class);
    when(game.getEmulator()).thenReturn(mock(GameEmulator.class));
    when(game.getRom()).thenReturn("twst_405");
    when(folderLookupService.getAltColorFolders(game, "twst_405")).thenReturn(List.of(serumFolder, vniFolder));
  }

  @Test
  void colorizationOfBothFoldersIsListed() throws Exception {
    create(serumFolder, "twst_405.cRZ");
    create(vniFolder, "pin2dmd.pal");
    create(vniFolder, "pin2dmd.vni");

    AltColor altColor = altColorService.getAltColor(game);

    assertThat(altColor.isAvailable()).isTrue();
    assertThat(altColor.getFiles()).containsExactlyInAnyOrder("twst_405.cRZ", "pin2dmd.pal", "pin2dmd.vni");
  }

  @Test
  void colorizationOfOneFolderIsListed() throws Exception {
    create(serumFolder, "twst_405.cRZ");

    AltColor altColor = altColorService.getAltColor(game);

    assertThat(altColor.isAvailable()).isTrue();
    assertThat(altColor.getFiles()).containsExactly("twst_405.cRZ");
    assertThat(altColorService.getAltColorFolder(game)).isEqualTo(serumFolder);
  }

  @Test
  void installedFilesGoToTheFolderVpxReadsThemFrom() throws Exception {
    when(folderLookupService.getSerumFolder(game, "twst_405")).thenReturn(serumFolder);
    when(folderLookupService.getVniFolder(game, "twst_405")).thenReturn(vniFolder);
    File download = tempDir.resolve("twst_405.cRZ").toFile();
    Files.writeString(download.toPath(), "serum");

    altColorService.installAltColorFromFile(game, download);

    assertThat(new File(serumFolder, "twst_405.cRZ")).exists();
    assertThat(vniFolder).doesNotExist();

    File pal = tempDir.resolve("colors.pal").toFile();
    Files.writeString(pal.toPath(), "pal");

    altColorService.installAltColorFromFile(game, pal);

    assertThat(new File(vniFolder, "pin2dmd.pal")).exists();
    assertThat(new File(serumFolder, "pin2dmd.pal")).doesNotExist();
  }

  @Test
  void deleteRemovesTheFilesOfBothFolders() throws Exception {
    create(serumFolder, "twst_405.cRZ");
    create(vniFolder, "pin2dmd.vni");
    when(game.getId()).thenReturn(1);

    assertThat(altColorService.delete(game)).isTrue();

    assertThat(serumFolder.listFiles()).isEmpty();
    assertThat(vniFolder.listFiles()).isEmpty();
  }

  private void create(File folder, String name) throws Exception {
    folder.mkdirs();
    new File(folder, name).createNewFile();
  }
}
