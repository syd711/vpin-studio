package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.validation.GameEmulatorValidationCode;
import de.mephisto.vpin.restclient.validation.ValidationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GameEmulatorValidationServiceTest {

  @InjectMocks
  private GameEmulatorValidationService service;

  private GameEmulator emulator;
  private File existingFolder;
  private File missingFolder;

  @BeforeEach
  void setUp() {
    emulator = mock(GameEmulator.class);
    existingFolder = mock(File.class);
    missingFolder = mock(File.class);
    when(existingFolder.exists()).thenReturn(true);
    when(missingFolder.exists()).thenReturn(false);
  }

  // ---- Installation directory ----

  @Test
  void validate_addsNoInstallDir_whenVpxAndInstallDirEmpty() {
    when(emulator.isVpxEmulator()).thenReturn(true);
    when(emulator.isFpEmulator()).thenReturn(false);
    when(emulator.isMameEmulator()).thenReturn(false);
    when(emulator.getType()).thenReturn(EmulatorType.VisualPinball);
    when(emulator.getInstallationDirectory()).thenReturn("");
    when(emulator.getGameExt()).thenReturn("vpx");
    when(emulator.getGamesDirectory()).thenReturn(null);
    when(emulator.getRomDirectory()).thenReturn(null);
    when(emulator.getMediaDirectory()).thenReturn(null);

    List<ValidationState> result = service.validate(FrontendType.Popper, emulator, false);

    assertTrue(result.stream().anyMatch(s -> s.getCode() == GameEmulatorValidationCode.CODE_NO_INSTALLATION_DIRECTORY));
  }

  @Test
  void validate_addsNoInstallDir_whenVpxAndInstallFolderMissing() {
    when(emulator.isVpxEmulator()).thenReturn(true);
    when(emulator.isFpEmulator()).thenReturn(false);
    when(emulator.isMameEmulator()).thenReturn(false);
    when(emulator.getType()).thenReturn(EmulatorType.VisualPinball);
    when(emulator.getInstallationDirectory()).thenReturn("C:/VPX");
    when(emulator.getInstallationFolder()).thenReturn(missingFolder);
    when(emulator.getGameExt()).thenReturn("vpx");
    when(emulator.getGamesDirectory()).thenReturn(null);
    when(emulator.getRomDirectory()).thenReturn(null);
    when(emulator.getMediaDirectory()).thenReturn(null);

    List<ValidationState> result = service.validate(FrontendType.Popper, emulator, false);

    assertTrue(result.stream().anyMatch(s -> s.getCode() == GameEmulatorValidationCode.CODE_NO_INSTALLATION_DIRECTORY));
  }

  // ---- Game extension ----

  @Test
  void validate_addsNoGameExtension_whenPopperAndExtensionEmpty() {
    when(emulator.isVpxEmulator()).thenReturn(true);
    when(emulator.isFpEmulator()).thenReturn(false);
    when(emulator.isMameEmulator()).thenReturn(false);
    when(emulator.getType()).thenReturn(EmulatorType.VisualPinball);
    when(emulator.getInstallationDirectory()).thenReturn("C:/VPX");
    when(emulator.getInstallationFolder()).thenReturn(existingFolder);
    when(emulator.isZaccariaEmulator()).thenReturn(false);
    when(emulator.getGameExt()).thenReturn("");
    when(emulator.getGamesDirectory()).thenReturn(null);
    when(emulator.getRomDirectory()).thenReturn(null);
    when(emulator.getMediaDirectory()).thenReturn(null);

    List<ValidationState> result = service.validate(FrontendType.Popper, emulator, false);

    assertTrue(result.stream().anyMatch(s -> s.getCode() == GameEmulatorValidationCode.CODE_NO_GAME_EXTENSION));
  }

  @Test
  void validate_noGameExtensionError_whenZaccariaEmulator() {
    when(emulator.isVpxEmulator()).thenReturn(false);
    when(emulator.isFpEmulator()).thenReturn(false);
    when(emulator.isMameEmulator()).thenReturn(false);
    when(emulator.getType()).thenReturn(EmulatorType.Zaccaria);
    when(emulator.isZaccariaEmulator()).thenReturn(true);
    when(emulator.getGameExt()).thenReturn("");
    when(emulator.getGamesDirectory()).thenReturn(null);
    when(emulator.getRomDirectory()).thenReturn(null);
    when(emulator.getMediaDirectory()).thenReturn(null);

    List<ValidationState> result = service.validate(FrontendType.Popper, emulator, false);

    assertFalse(result.stream().anyMatch(s -> s.getCode() == GameEmulatorValidationCode.CODE_NO_GAME_EXTENSION));
  }

  // ---- ROM directory ----

  @Test
  void validate_addsInvalidRomsFolder_whenRomDirNonEmptyAndMissing() {
    when(emulator.isVpxEmulator()).thenReturn(false);
    when(emulator.isFpEmulator()).thenReturn(false);
    when(emulator.isMameEmulator()).thenReturn(false);
    when(emulator.getType()).thenReturn(EmulatorType.OTHER);
    when(emulator.getInstallationDirectory()).thenReturn("C:/VPX");
    when(emulator.getInstallationFolder()).thenReturn(existingFolder);
    when(emulator.isZaccariaEmulator()).thenReturn(false);
    when(emulator.getGameExt()).thenReturn("vpx");
    when(emulator.getGamesDirectory()).thenReturn(null);
    when(emulator.getRomDirectory()).thenReturn("C:/NonExistentROMs");
    when(emulator.getMediaDirectory()).thenReturn(null);

    List<ValidationState> result = service.validate(FrontendType.Popper, emulator, false);

    assertTrue(result.stream().anyMatch(s -> s.getCode() == GameEmulatorValidationCode.CODE_INVALID_ROMS_FOLDER));
  }

  @Test
  void validate_noRomError_whenRomDirEmpty() {
    when(emulator.isVpxEmulator()).thenReturn(false);
    when(emulator.isFpEmulator()).thenReturn(false);
    when(emulator.isMameEmulator()).thenReturn(false);
    when(emulator.getType()).thenReturn(EmulatorType.OTHER);
    when(emulator.getInstallationDirectory()).thenReturn("C:/VPX");
    when(emulator.getInstallationFolder()).thenReturn(existingFolder);
    when(emulator.isZaccariaEmulator()).thenReturn(false);
    when(emulator.getGameExt()).thenReturn("vpx");
    when(emulator.getGamesDirectory()).thenReturn(null);
    when(emulator.getRomDirectory()).thenReturn("");
    when(emulator.getMediaDirectory()).thenReturn(null);

    List<ValidationState> result = service.validate(FrontendType.Popper, emulator, false);

    assertFalse(result.stream().anyMatch(s -> s.getCode() == GameEmulatorValidationCode.CODE_INVALID_ROMS_FOLDER));
  }

  // ---- Media directory ----

  @Test
  void validate_addsInvalidMediaFolder_whenMediaDirNonEmptyAndMissing() {
    when(emulator.isVpxEmulator()).thenReturn(false);
    when(emulator.isFpEmulator()).thenReturn(false);
    when(emulator.isMameEmulator()).thenReturn(false);
    when(emulator.getType()).thenReturn(EmulatorType.OTHER);
    when(emulator.getInstallationDirectory()).thenReturn("C:/VPX");
    when(emulator.getInstallationFolder()).thenReturn(existingFolder);
    when(emulator.isZaccariaEmulator()).thenReturn(false);
    when(emulator.getGameExt()).thenReturn("vpx");
    when(emulator.getGamesDirectory()).thenReturn(null);
    when(emulator.getRomDirectory()).thenReturn(null);
    when(emulator.getMediaDirectory()).thenReturn("C:/NonExistentMedia");

    List<ValidationState> result = service.validate(FrontendType.Popper, emulator, false);

    assertTrue(result.stream().anyMatch(s -> s.getCode() == GameEmulatorValidationCode.CODE_INVALID_MEDIA_FOLDER));
  }

  // ---- findFirst short-circuit ----

  @Test
  void validate_returnsOnFirstError_whenFindFirstTrue() {
    when(emulator.isVpxEmulator()).thenReturn(true);
    when(emulator.isFpEmulator()).thenReturn(false);
    when(emulator.isMameEmulator()).thenReturn(false);
    when(emulator.getType()).thenReturn(EmulatorType.VisualPinball);
    when(emulator.getInstallationDirectory()).thenReturn("");

    List<ValidationState> result = service.validate(FrontendType.Popper, emulator, true);

    assertEquals(1, result.size());
    assertEquals(GameEmulatorValidationCode.CODE_NO_INSTALLATION_DIRECTORY, result.get(0).getCode());
  }

  // ---- Clean emulator ----

  @Test
  void validate_returnsEmpty_whenNonEmulatorType() {
    when(emulator.isVpxEmulator()).thenReturn(false);
    when(emulator.isFpEmulator()).thenReturn(false);
    when(emulator.isMameEmulator()).thenReturn(false);
    when(emulator.getType()).thenReturn(EmulatorType.ZenFX);
    when(emulator.isZaccariaEmulator()).thenReturn(false);
    when(emulator.getGameExt()).thenReturn("pxp");
    when(emulator.getGamesDirectory()).thenReturn(null);
    when(emulator.getRomDirectory()).thenReturn(null);
    when(emulator.getMediaDirectory()).thenReturn(null);

    List<ValidationState> result = service.validate(FrontendType.Popper, emulator, false);

    assertTrue(result.isEmpty());
  }
}
