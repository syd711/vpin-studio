package de.mephisto.vpin.server.components.facades;

import de.mephisto.githubloader.GithubRelease;
import de.mephisto.githubloader.GithubReleaseFactory;
import de.mephisto.githubloader.ReleaseArtifact;
import de.mephisto.githubloader.ReleaseArtifactActionLog;
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

public class SerumComponent implements ComponentFacade {
  @NotNull
  @Override
  public String[] getDiffList() {
    return new String[]{".dll"};
  }

  @NotNull
  @Override
  public String getReleasesUrl() {
    return "https://github.com/zesinger/libserum/releases";
  }

  @Override
  public List<GithubRelease> loadReleases() throws IOException {
    return GithubReleaseFactory.loadReleases(getReleasesUrl(), Collections.emptyList(), Arrays.asList("Debug", "Source", "linux", "sc-", "macos", "android", "arm"));
  }

  @NotNull
  @Override
  public File getTargetFolder(@NotNull GameEmulator gameEmulator) {
    return gameEmulator.getMameFolder();
  }

  @Nullable
  @Override
  public Date getModificationDate(@NonNull GameEmulator gameEmulator) {
    File testExe = new File(gameEmulator.getMameFolder(), "serum_test.exe");
    if (testExe.exists()) {
      return new Date(testExe.lastModified());
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
    return Arrays.asList("serum64.dll", "serum.dll");
  }
}
