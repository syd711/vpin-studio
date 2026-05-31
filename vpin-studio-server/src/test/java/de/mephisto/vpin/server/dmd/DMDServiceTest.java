package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.restclient.dmd.DMDPackage;
import de.mephisto.vpin.restclient.dmd.DMDPackageTypes;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.vpinmame.VPinMameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DMDServiceTest {

  @Mock
  private VPinMameService vPinMameService;

  @InjectMocks
  private DMDService service;

  @TempDir
  Path tempDir;

  private Game buildGame(DMDPackageTypes type, String projectFolder) throws IOException {
    Game game = mock(Game.class);
    File vpxFile = tempDir.resolve("game.vpx").toFile();
    vpxFile.createNewFile();
    when(game.getGameFile()).thenReturn(vpxFile);
    when(game.getDMDType()).thenReturn(type);
    when(game.getDMDProjectFolder()).thenReturn(projectFolder);
    return game;
  }

  // ---- getDmdFolder ----

  @Test
  void getDmdFolder_returnsSubfolderNamedAfterProjectFolder() throws IOException {
    Game game = buildGame(DMDPackageTypes.UltraDMD, "MyDMD");

    File folder = service.getDmdFolder(game);

    assertThat(folder.getName()).isEqualTo("MyDMD");
    assertThat(folder.getParentFile()).isEqualTo(tempDir.toFile());
  }

  // ---- getDMDPackage ----

  @Test
  void getDMDPackage_returnsNullForStandardType() {
    Game game = mock(Game.class);
    when(game.getDMDType()).thenReturn(DMDPackageTypes.Standard);

    assertThat(service.getDMDPackage(game)).isNull();
  }

  @Test
  void getDMDPackage_returnsNullWhenTypeIsNull() {
    Game game = mock(Game.class);
    when(game.getDMDType()).thenReturn(null);

    assertThat(service.getDMDPackage(game)).isNull();
  }

  @Test
  void getDMDPackage_returnsNullWhenProjectFolderIsBlank() {
    Game game = mock(Game.class);
    when(game.getDMDType()).thenReturn(DMDPackageTypes.UltraDMD);
    when(game.getDMDProjectFolder()).thenReturn(null);

    assertThat(service.getDMDPackage(game)).isNull();
  }

  @Test
  void getDMDPackage_addsValidationErrorWhenFolderMissing() throws IOException {
    Game game = buildGame(DMDPackageTypes.UltraDMD, "MissingFolder");

    DMDPackage pkg = service.getDMDPackage(game);

    assertThat(pkg).isNotNull();
    assertThat(pkg.getValidationStates()).isNotEmpty();
  }

  @Test
  void getDMDPackage_returnsPackageWithFilesForExistingFolder() throws IOException {
    Path dmdDir = tempDir.resolve("UltraDMD");
    Files.createDirectory(dmdDir);
    Files.writeString(dmdDir.resolve("serum.cRZ"), "data");

    Game game = buildGame(DMDPackageTypes.UltraDMD, "UltraDMD");

    DMDPackage pkg = service.getDMDPackage(game);

    assertThat(pkg).isNotNull();
    assertThat(pkg.getValidationStates()).isEmpty();
    assertThat(pkg.getFiles()).contains("serum.cRZ");
    assertThat(pkg.getSize()).isGreaterThan(0);
  }

  @Test
  void getDMDPackage_setsCorrectPackageType() throws IOException {
    Path dmdDir = tempDir.resolve("FlexDMD");
    Files.createDirectory(dmdDir);

    Game game = buildGame(DMDPackageTypes.FlexDMD, "FlexDMD");

    DMDPackage pkg = service.getDMDPackage(game);

    assertThat(pkg).isNotNull();
    assertThat(pkg.getDmdPackageTypes()).isEqualTo(DMDPackageTypes.FlexDMD);
  }

  // ---- delete ----

  @Test
  void delete_returnsFalseWhenNoDMDPackage() {
    Game game = mock(Game.class);
    when(game.getDMDType()).thenReturn(null);

    assertThat(service.delete(game)).isFalse();
  }

  @Test
  void delete_returnsFalseWhenFolderDoesNotExist() throws IOException {
    Game game = buildGame(DMDPackageTypes.UltraDMD, "NonExistent");

    assertThat(service.delete(game)).isFalse();
  }

  @Test
  void delete_returnsTrueAndRemovesFolderWhenExists() throws IOException {
    Path dmdDir = tempDir.resolve("ToDelete");
    Files.createDirectory(dmdDir);
    Files.writeString(dmdDir.resolve("file.txt"), "content");

    Game game = buildGame(DMDPackageTypes.UltraDMD, "ToDelete");

    boolean result = service.delete(game);

    assertThat(result).isTrue();
    assertThat(dmdDir.toFile()).doesNotExist();
  }

  // ---- clearCache ----

  @Test
  void clearCache_alwaysReturnsTrue() {
    assertThat(service.clearCache()).isTrue();
  }
}
