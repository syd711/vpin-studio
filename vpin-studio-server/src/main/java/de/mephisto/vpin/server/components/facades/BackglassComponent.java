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

public class BackglassComponent implements ComponentFacade {

  @NonNull
  @Override
  public String[] getDiffList() {
    return new String[]{".dll", ".cmd", ".exe"};
  }

  @NotNull
  @Override
  public String getReleasesUrl() {
    return "https://github.com/vpinball/b2s-backglass/releases";
  }

  @Override
  public GithubRelease loadRelease() throws IOException {
    return GithubReleaseFactory.loadRelease(getReleasesUrl(), Collections.emptyList(), Arrays.asList("Source"));
  }

  @NonNull
  @Override
  public File getTargetFolder(@NonNull GameEmulator gameEmulator) {
    return gameEmulator.getTablesFolder();
  }

  @Nullable
  @Override
  public Date getModificationDate(@NonNull GameEmulator gameEmulator) {
    File file = new File(gameEmulator.getTablesFolder(), "B2SBackglassServer.dll");
    if (file.exists()) {
      return new Date(file.lastModified());
    }
    return null;
  }

  @Nullable
  @Override
  public List<String> getExclusionList() {
    return Collections.emptyList();
  }

  @Override
  public boolean isSkipRootFolder() {
    return true;
  }
}
