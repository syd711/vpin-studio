package de.mephisto.vpin.server.components.facades;

import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.connectors.github.GithubReleaseFactory;
import de.mephisto.vpin.server.games.GameEmulator;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class VpxComponent implements ComponentFacade {
  @NotNull
  @Override
  public String[] getDiffList() {
    return new String[]{".vbs", ".dll", ".exe"};
  }

  @NotNull
  @Override
  public String getReleasesUrl() {
    return "https://github.com/vpinball/vpinball/releases";
  }

  @Override
  public List<GithubRelease> loadReleases() throws IOException {
    return GithubReleaseFactory.loadReleases(getReleasesUrl(), Collections.emptyList(), Arrays.asList("Debug", "Source", "linux", "sc-", "macos", "android"));
  }

  @NotNull
  @Override
  public File getTargetFolder(@NotNull GameEmulator gameEmulator) {
    return gameEmulator.getTablesFolder()!=null? gameEmulator.getTablesFolder().getParentFile(): null;
  }

  @Nullable
  @Override
  public Date getModificationDate(@NonNull GameEmulator gameEmulator) {
    File setupExe = new File(gameEmulator.getTablesFolder().getParentFile(), "VPinballX64.exe");
    if (!setupExe.exists()) {
      setupExe = new File(gameEmulator.getTablesFolder().getParentFile(), "VPinballX.exe");
    }
    if (setupExe.exists()) {
      return new Date(setupExe.lastModified());
    }
    return null;
  }

  @Nullable
  @Override
  public List<String> getExclusionList() {
    return Collections.emptyList();
  }

  @Override
  public List<String> getRootFolderIndicators() {
    return Arrays.asList("VPinballX64.exe", "VPinballX.exe", "VPinballX_GL.exe");
  }
}
