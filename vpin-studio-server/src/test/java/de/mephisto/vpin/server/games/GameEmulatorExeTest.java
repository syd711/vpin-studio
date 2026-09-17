package de.mephisto.vpin.server.games;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class GameEmulatorExeTest {

  @TempDir
  File tempDir;

  @Test
  void getExe_relativeName_isInsideInstallationFolder() {
    GameEmulator emulator = new GameEmulator();
    emulator.setInstallationDirectory(tempDir.getAbsolutePath());
    emulator.setExeName("VPinballX_BGFX");

    assertThat(emulator.getExe()).isEqualTo(new File(tempDir, "VPinballX_BGFX"));
  }

  @Test
  void getExe_absoluteName_isUsedAsIs() {
    File launcher = new File(tempDir, "bin/vpx-launch.sh");
    GameEmulator emulator = new GameEmulator();
    emulator.setInstallationDirectory(new File(tempDir, "vpx").getAbsolutePath());
    emulator.setExeName(launcher.getAbsolutePath());

    assertThat(emulator.getExe()).isEqualTo(launcher);
  }
}
