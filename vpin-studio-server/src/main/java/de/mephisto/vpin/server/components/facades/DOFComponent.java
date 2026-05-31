package de.mephisto.vpin.server.components.facades;

import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.connectors.github.GithubReleaseFactory;
import de.mephisto.vpin.connectors.github.ReleaseArtifact;
import de.mephisto.vpin.connectors.github.ReleaseArtifactActionLog;
import de.mephisto.vpin.server.dof.DOFService;
import de.mephisto.vpin.server.frontend.FrontendService;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class DOFComponent implements ComponentFacade {

  @Autowired
  private DOFService dofService;

  @Autowired
  private FrontendService frontendService;

  @NonNull
  @Override
  public String[] getDiffList() {
    return new String[]{".dll"};
  }

  @NonNull
  @Override
  public String getReleasesUrl() {
    return "https://github.com/mjrgh/DirectOutput/releases";
  }

  @Override
  public List<GithubRelease> loadReleases() throws IOException {
    return GithubReleaseFactory.loadReleases(getReleasesUrl(), Collections.emptyList(), Arrays.asList("Debug", "Source", ".msi"));
  }

  @Nullable
  @Override
  public File getTargetFolder() {
    return dofService.getInstallationFolder();
  }

  @Nullable
  @Override
  public OffsetDateTime getModificationDate() {
    if (dofService.getInstallationFolder() != null) {
      File testExe = new File(dofService.getInstallationFolder(), "config/tablemappings.xml");
      if (testExe.exists()) {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(testExe.lastModified()), ZoneId.systemDefault());
      }
    }
    return null;
  }

  @Override
  public boolean isInstalled() {
    return dofService.isValid();
  }

  @NonNull
  @Override
  public List<String> getExcludedFilenames() {
    return Arrays.asList(".ini", ".xml", ".png");
  }

  @Override
  public void preProcess(@NonNull ReleaseArtifact releaseArtifact, @NonNull ReleaseArtifactActionLog install) {
    frontendService.killFrontend();
  }

  @Override
  public void postProcess(@NonNull ReleaseArtifact releaseArtifact, @NonNull ReleaseArtifactActionLog install) {

  }

  @Override
  public List<String> getRootFolderInArchiveIndicators() {
    return List.of("DirectOutput.dll");
  }
}
