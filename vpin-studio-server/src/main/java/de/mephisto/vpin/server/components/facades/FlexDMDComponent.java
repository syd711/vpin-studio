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
import java.util.List;

public class FlexDMDComponent implements ComponentFacade {

  @NotNull
  @Override
  public String[] getDiffList() {
    return new String[]{".dll", ".exe"};
  }

  @NotNull
  @Override
  public String getReleasesUrl() {
    return "https://github.com/vbousquet/flexdmd/releases";
  }

  @Override
  public List<GithubRelease> loadReleases() throws IOException {
    return Collections.singletonList(GithubReleaseFactory.loadRelease(getReleasesUrl(), Collections.emptyList(), Arrays.asList("Source")));
  }

  @NotNull
  @Override
  public File getTargetFolder(@NotNull GameEmulator gameEmulator) {
    return gameEmulator.getMameFolder();
  }

  @Nullable
  @Override
  public Date getModificationDate(@NonNull GameEmulator gameEmulator) {
    File file = new File(gameEmulator.getMameFolder(), "FlexDMD.dll");
    if (file.exists()) {
      return new Date(file.lastModified());
    }
    return null;
  }

  @Nullable
  @Override
  public List<String> getExclusionList() {
    return Arrays.asList("FlexDMD.log.config");
  }

  @Override
  public boolean isSkipRootFolder() {
    return true;
  }
}
