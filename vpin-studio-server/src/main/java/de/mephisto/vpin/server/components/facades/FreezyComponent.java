package de.mephisto.vpin.server.components.facades;

import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.connectors.github.GithubReleaseFactory;
import de.mephisto.vpin.connectors.github.ReleaseArtifact;
import de.mephisto.vpin.connectors.github.ReleaseArtifactActionLog;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.server.mame.MameService;
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
public class FreezyComponent implements ComponentFacade {

  private final static List<String> INVALID_MAME_FILES = Arrays.asList("serum.dll", "serum.exp", "serum.lib", "serum64.dll", "serum64.exp", "serum64.lib");

  @Autowired
  private MameService mameService;

  @NonNull
  @Override
  public String[] getDiffList() {
    return new String[]{".dll"};
  }

  @NonNull
  @Override
  public String getReleasesUrl() {
    return "https://github.com/freezy/dmd-extensions/releases";
  }

  @Override
  public List<GithubRelease> loadReleases() throws IOException {
    return Arrays.asList(GithubReleaseFactory.loadRelease(getReleasesUrl(), Collections.emptyList(), Arrays.asList("Source", ".msi")));
  }

  @NonNull
  @Override
  public File getTargetFolder() {
    return mameService.getMameFolder();
  }

  @Nullable
  @Override
  public Date getModificationDate() {
    File file = new File(mameService.getMameFolder(), "DmdDevice64.dll");
    if (!file.exists()) {
      file = new File(mameService.getMameFolder(), "DmdDevice.dll");
    }
    if (file.exists()) {
      return new Date(file.lastModified());
    }
    return null;
  }

  @Override
  public void postProcess(@NonNull ReleaseArtifact releaseArtifact, @NonNull ReleaseArtifactActionLog install) {
    for (String deleteFile : INVALID_MAME_FILES) {
      FileUtils.delete(new File(mameService.getMameFolder(), deleteFile));
    }
  }

  @Nullable
  @Override
  public List<String> getExcludedFilenames() {
    return Arrays.asList("DmdDevice.log.config", "DmdDevice.ini", "dmdext.log.config");
  }

  @Override
  public List<String> getRootFolderInArchiveIndicators() {
    return Arrays.asList("DmdDevice.ini");
  }
}
