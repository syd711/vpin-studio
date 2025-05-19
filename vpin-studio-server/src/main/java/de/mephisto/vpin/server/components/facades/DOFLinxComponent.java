package de.mephisto.vpin.server.components.facades;

import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.connectors.github.GithubReleaseFactory;
import de.mephisto.vpin.connectors.github.ReleaseArtifact;
import de.mephisto.vpin.connectors.github.ReleaseArtifactActionLog;
import de.mephisto.vpin.server.doflinx.DOFLinxService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
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
  public File getTargetFolder() {
    return dofLinxService.getInstallationFolder();
  }

  @Nullable
  @Override
  public Date getModificationDate() {
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

  @NonNull
  @Override
  public List<String> getExcludedFilenames() {
    return Arrays.asList(".ini", ".INI", ".log", ".bak");
  }

  @NonNull
  @Override
  public List<String> getIncludedFilenames() {
    return Arrays.asList("Sample INI files/");
  }

  @Override
  public void preProcess(@NonNull ReleaseArtifact releaseArtifact, @NonNull ReleaseArtifactActionLog install) {
    dofLinxService.killDOFLinx();
  }

  @Override
  public void postProcess(@NonNull ReleaseArtifact releaseArtifact, @NonNull ReleaseArtifactActionLog install) {
    if (dofLinxService.isValid()) {
      File starter1 = new File(dofLinxService.getInstallationFolder(), "Starter 32 bit");
      if (starter1.exists()) {
        FileUtils.deleteFolder(starter1);
      }
      File starter2 = new File(dofLinxService.getInstallationFolder(), "Starter 64 bit");
      if (starter2.exists()) {
        FileUtils.deleteFolder(starter2);
      }
    }
  }

  @Override
  public List<String> getRootFolderInArchiveIndicators() {
    return Arrays.asList("HELP.txt", "DOFLinx.vbs");
  }
}
