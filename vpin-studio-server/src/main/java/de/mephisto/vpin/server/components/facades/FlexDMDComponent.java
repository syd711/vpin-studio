package de.mephisto.vpin.server.components.facades;

import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.connectors.github.GithubReleaseFactory;
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
public class FlexDMDComponent implements ComponentFacade {

  @Autowired
  private MameService mameService;

  @NonNull
  @Override
  public String[] getDiffList() {
    return new String[]{".dll", ".exe"};
  }

  @NonNull
  @Override
  public String getReleasesUrl() {
    return "https://github.com/vbousquet/flexdmd/releases";
  }

  @Override
  public List<GithubRelease> loadReleases() throws IOException {
    return Collections.singletonList(GithubReleaseFactory.loadRelease(getReleasesUrl(), Collections.emptyList(), Arrays.asList("Source")));
  }

  @NonNull
  @Override
  public File getTargetFolder() {
    return mameService.getMameFolder();
  }

  @Nullable
  @Override
  public Date getModificationDate() {
    File file = new File(mameService.getMameFolder(), "FlexDMD.dll");
    if (file.exists()) {
      return new Date(file.lastModified());
    }
    return null;
  }

  @NonNull
  @Override
  public List<String> getExcludedFilenames() {
    return Arrays.asList("FlexDMD.log.config");
  }

  @Override
  public List<String> getRootFolderInArchiveIndicators() {
    return Arrays.asList("FlexDMDUI.exe");
  }
}
