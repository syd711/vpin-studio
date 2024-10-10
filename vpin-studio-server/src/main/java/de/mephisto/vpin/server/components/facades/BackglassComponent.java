package de.mephisto.vpin.server.components.facades;

import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.connectors.github.GithubReleaseFactory;
import de.mephisto.vpin.server.games.GameEmulator;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class BackglassComponent implements ComponentFacade {

  @NonNull
  @Override
  public String[] getDiffList() {
    return new String[]{".dll", ".cmd", ".exe"};
  }

  @NonNull
  @Override
  public String getReleasesUrl() {
    return "https://github.com/vpinball/b2s-backglass/releases";
  }

  @Override
  public List<GithubRelease> loadReleases() throws IOException {
    return Collections.singletonList(GithubReleaseFactory.loadRelease(getReleasesUrl(), Collections.emptyList(), Arrays.asList("Source")));
  }

  @Nullable
  @Override
  public File getTargetFolder(@NonNull GameEmulator gameEmulator) {
    return gameEmulator.getBackglassServerDirectory();
  }

  @Nullable
  @Override
  public Date getModificationDate(@NonNull GameEmulator gameEmulator) {
    File file = new File(gameEmulator.getBackglassServerDirectory(), "B2S_ScreenResIdentifier.exe");
    if (file.exists()) {
      return new Date(file.lastModified());
    }
    return null;
  }

  @Nullable
  @Override
  public List<String> getExclusionList() {
    return Arrays.asList("B2S_ScreenResIdentifier.exe.config", "B2SBackglassServerEXE.exe.config");
  }

  @Override
  public List<String> getRootFolderIndicators() {
    return Arrays.asList("README.txt");
  }
}
