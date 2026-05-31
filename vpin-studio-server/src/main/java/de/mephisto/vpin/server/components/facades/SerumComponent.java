package de.mephisto.vpin.server.components.facades;

import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.connectors.github.GithubReleaseFactory;
import de.mephisto.vpin.server.vpinmame.VPinMameService;
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
public class SerumComponent implements ComponentFacade {
  @Autowired
  private VPinMameService vPinMameService;

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
    return vPinMameService.getMameFolder();
  }

  @Nullable
  @Override
  public OffsetDateTime getModificationDate() {
    File testExe = new File(vPinMameService.getMameFolder(), "serum_test.exe");
    if (testExe.exists()) {
      return OffsetDateTime.ofInstant(Instant.ofEpochMilli(testExe.lastModified()), ZoneId.systemDefault());
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
