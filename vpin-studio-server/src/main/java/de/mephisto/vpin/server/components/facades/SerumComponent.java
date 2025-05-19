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
public class SerumComponent implements ComponentFacade {
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
    return "https://github.com/zesinger/libserum/releases";
  }

  @Override
  public List<GithubRelease> loadReleases() throws IOException {
    return GithubReleaseFactory.loadReleases(getReleasesUrl(), Collections.emptyList(), Arrays.asList("Debug", "Source", "linux", "sc-", "macos", "android", "arm"));
  }

  @NonNull
  @Override
  public File getTargetFolder() {
    return mameService.getMameFolder();
  }

  @Nullable
  @Override
  public Date getModificationDate() {
    File testExe = new File(mameService.getMameFolder(), "serum_test.exe");
    if (testExe.exists()) {
      return new Date(testExe.lastModified());
    }
    return null;
  }

  @NonNull
  @Override
  public List<String> getExcludedFilenames() {
    return Collections.emptyList();
  }

  @Override
  public List<String> getRootFolderInArchiveIndicators() {
    return Arrays.asList("serum64.dll", "serum.dll");
  }
}
