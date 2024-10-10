package de.mephisto.vpin.server.components.facades;

import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.connectors.github.GithubReleaseFactory;
import de.mephisto.vpin.server.doflinx.DOFLinxService;
import de.mephisto.vpin.server.games.GameEmulator;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class DOFLinxComponent implements ComponentFacade {

  @Autowired
  private DOFLinxService dofLinxService;

  @NonNull
  @Override
  public String[] getDiffList() {
    return new String[]{".dll"};
  }

  @NonNull
  @Override
  public String getReleasesUrl() {
    return "https://github.com/DOFLinx/DOFLinx/releases";
  }

  @Override
  public List<GithubRelease> loadReleases() throws IOException {
    return GithubReleaseFactory.loadReleases(getReleasesUrl(), Collections.emptyList(), Arrays.asList("Debug", "Source"));
  }

  @Nullable
  @Override
  public File getTargetFolder(@NonNull GameEmulator gameEmulator) {
    return dofLinxService.getInstallationFolder();
  }

  @Nullable
  @Override
  public Date getModificationDate(@NonNull GameEmulator gameEmulator) {
    if (dofLinxService.getInstallationFolder() != null) {
      File testExe = new File(dofLinxService.getInstallationFolder(), "DOFLinx.exe");
      if (testExe.exists()) {
        return new Date(testExe.lastModified());
      }
    }
    return null;
  }

  @Override
  public boolean isInstalled() {
    return dofLinxService.isValid();
  }

  @Nullable
  @Override
  public List<String> getExcludedFilenames() {
    return Arrays.asList(".ini", ".INI", ".log");
  }

  @NotNull
  @Override
  public List<String> getIncludedFilenames() {
    return Arrays.asList("Sample INI files/");
  }

  @Override
  public List<String> getRootFolderInArchiveIndicators() {
    return Arrays.asList("HELP.txt", "DOFLinx.vbs");
  }
}
