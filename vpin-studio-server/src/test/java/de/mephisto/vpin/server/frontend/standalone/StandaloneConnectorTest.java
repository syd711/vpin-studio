package de.mephisto.vpin.server.frontend.standalone;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class StandaloneConnectorTest {

  @TempDir
  File installFolder;

  private File executable(File folder, String name) throws IOException {
    File file = new File(folder, name);
    file.getParentFile().mkdirs();
    file.createNewFile();
    file.setExecutable(true);
    return file;
  }

  @Test
  void resolveExe_windows_findsVPinballExe() throws IOException {
    executable(installFolder, "VPinballX.exe");
    File x64 = executable(installFolder, "VPinballX64.exe");

    assertThat(StandaloneConnector.resolveExe(installFolder, null, true)).isEqualTo(x64);
  }

  @Test
  @DisabledOnOs(OS.WINDOWS)
  void resolveExe_standalone_prefersBgfxBuild() throws IOException {
    executable(installFolder, "VPinballX_GL");
    File bgfx = executable(installFolder, "VPinballX_BGFX");
    // the Windows binaries are not considered
    executable(installFolder, "VPinballX64.exe");

    assertThat(StandaloneConnector.resolveExe(installFolder, null, false)).isEqualTo(bgfx);
  }

  @Test
  @DisabledOnOs(OS.WINDOWS)
  void resolveExe_standalone_ignoresFilesThatAreNotExecutable() throws IOException {
    File bgfx = executable(installFolder, "VPinballX_BGFX");
    bgfx.setExecutable(false);
    File gl = executable(installFolder, "VPinballX_GL");

    assertThat(StandaloneConnector.resolveExe(installFolder, null, false)).isEqualTo(gl);
  }

  @Test
  void resolveExe_configuredAbsoluteLauncher_isUsed(@TempDir File binFolder) throws IOException {
    executable(installFolder, "VPinballX_BGFX");
    File wrapper = executable(binFolder, "vpx-launch.sh");

    assertThat(StandaloneConnector.resolveExe(installFolder, wrapper.getAbsolutePath(), false)).isEqualTo(wrapper);
  }

  @Test
  void resolveExe_configuredRelativeLauncher_isResolvedAgainstInstallFolder() throws IOException {
    File build = executable(installFolder, "builds/5436/VPinballX_BGFX");

    assertThat(StandaloneConnector.resolveExe(installFolder, "builds/5436/VPinballX_BGFX", false)).isEqualTo(build);
  }

  @Test
  @DisabledOnOs(OS.WINDOWS)
  void resolveExe_missingConfiguredLauncher_fallsBackToInstallFolder() throws IOException {
    File bgfx = executable(installFolder, "VPinballX_BGFX");

    assertThat(StandaloneConnector.resolveExe(installFolder, "/does/not/exist", false)).isEqualTo(bgfx);
  }

  @Test
  void resolveExe_nothingFound_returnsPlaceholderInInstallFolder() {
    File windows = StandaloneConnector.resolveExe(installFolder, null, true);
    File standalone = StandaloneConnector.resolveExe(installFolder, null, false);

    assertThat(windows).doesNotExist().hasName("vpx_not_found.exe").hasParent(installFolder);
    assertThat(standalone).doesNotExist().hasName("vpx_not_found").hasParent(installFolder);
  }
}
