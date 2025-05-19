package de.mephisto.vpin.server.components.facades;

import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.connectors.github.GithubReleaseFactory;
import de.mephisto.vpin.server.directb2s.BackglassService;
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
public class BackglassComponent implements ComponentFacade {

  @Autowired
  private BackglassService backglassService;


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
  public File getTargetFolder() {
    return backglassService.getBackglassServerFolder();
  }

  @Nullable
  @Override
  public Date getModificationDate() {
    File file = new File(backglassService.getBackglassServerFolder(), "B2S_ScreenResIdentifier.exe");
    if (file.exists()) {
      return new Date(file.lastModified());
    }
    return null;
  }

  @NonNull
  @Override
  public List<String> getExcludedFilenames() {
    return Arrays.asList("B2S_ScreenResIdentifier.exe.config", "B2SBackglassServerEXE.exe.config");
  }

  @Override
  public List<String> getRootFolderInArchiveIndicators() {
    return Arrays.asList("README.txt");
  }
}
