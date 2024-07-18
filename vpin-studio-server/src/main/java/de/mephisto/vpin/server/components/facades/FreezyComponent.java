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

public class FreezyComponent implements ComponentFacade {
  @NotNull
  @Override
  public String[] getDiffList() {
    return new String[]{".dll"};
  }

  @NotNull
  @Override
  public String getReleasesUrl() {
    return "https://github.com/freezy/dmd-extensions/releases";
  }

  @Override
  public List<GithubRelease> loadReleases() throws IOException {
    return Arrays.asList(GithubReleaseFactory.loadRelease(getReleasesUrl(), Collections.emptyList(), Arrays.asList("Source", ".msi")));
  }

  @NotNull
  @Override
  public File getTargetFolder(@NotNull GameEmulator gameEmulator) {
    return gameEmulator.getMameFolder();
  }

  @Nullable
  @Override
  public Date getModificationDate(@NonNull GameEmulator gameEmulator) {
    File file = new File(gameEmulator.getMameFolder(), "DmdDevice64.dll");
    if (!file.exists()) {
      file = new File(gameEmulator.getMameFolder(), "DmdDevice.dll");
    }
    if (file.exists()) {
      return new Date(file.lastModified());
    }
    return null;
  }

  @Nullable
  @Override
  public List<String> getExclusionList() {
    return Arrays.asList("DmdDevice.log.config", "DmdDevice.ini", "dmdext.log.config");
  }

  @Override
  public List<String> getRootFolderIndicators() {
    return Arrays.asList("DmdDevice.ini");
  }
}
