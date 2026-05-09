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
public class FlexDMDComponent implements ComponentFacade {

  @Autowired
  private VPinMameService vPinMameService;

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
    return GithubReleaseFactory.loadReleases(getReleasesUrl(), Collections.emptyList(), Arrays.asList("Source"));
  }

  @NonNull
  @Override
  public File getTargetFolder() {
    return vPinMameService.getMameFolder();
  }

  @Nullable
  @Override
  public OffsetDateTime getModificationDate() {
    File file = new File(vPinMameService.getMameFolder(), "FlexDMD.dll");
    if (file.exists()) {
      return OffsetDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault());
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
