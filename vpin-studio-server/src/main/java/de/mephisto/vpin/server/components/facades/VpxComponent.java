package de.mephisto.vpin.server.components.facades;

import de.mephisto.githubloader.GithubRelease;
import de.mephisto.githubloader.GithubReleaseFactory;
import de.mephisto.vpin.server.games.GameEmulator;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

public class VpxComponent implements ComponentFacade {
  @NotNull
  @Override
  public String[] getDiffList() {
    return new String[]{".vbs", ".dll"};
  }

  @NotNull
  @Override
  public String getReleasesUrl() {
    return "https://github.com/vpinball/vpinball/releases";
  }

  @Override
  public GithubRelease loadRelease() throws IOException {
    return GithubReleaseFactory.loadRelease(getReleasesUrl(), Collections.emptyList(), Arrays.asList("Debug", "Source"));
  }

  @NotNull
  @Override
  public File getTargetFolder(@NotNull GameEmulator gameEmulator) {
    return gameEmulator.getTablesFolder().getParentFile();
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
}
