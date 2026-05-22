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
import java.util.List;

@Service
public class VPinMAMEComponent implements ComponentFacade {

  @Autowired
  private VPinMameService vPinMameService;

  @NonNull
  @Override
  public String[] getDiffList() {
    return new String[]{"Setup64.exe", "Setup.exe", ".dll"};
  }

  @NonNull
  @Override
  public String getReleasesUrl() {
    return "https://github.com/vpinball/pinmame/releases";
  }

  @Override
  public List<GithubRelease> loadReleases() throws IOException {
    return GithubReleaseFactory.loadReleases(getReleasesUrl(), Arrays.asList("win-", "VPinMAME"), Arrays.asList("linux", "osx"));
  }

  @NonNull
  @Override
  public File getTargetFolder() {
    return vPinMameService.getMameFolder();
  }

  @Nullable
  @Override
  public OffsetDateTime getModificationDate() {
    File setupExe = new File(vPinMameService.getMameFolder(), "Setup64.exe");
    if (!setupExe.exists()) {
      setupExe = new File(vPinMameService.getMameFolder(), "Setup.exe");
    }
    if (setupExe.exists()) {
      return OffsetDateTime.ofInstant(Instant.ofEpochMilli(setupExe.lastModified()), ZoneId.systemDefault());
    }
    return null;
  }

  @NonNull
  @Override
  public List<String> getExcludedFilenames() {
    return Arrays.asList("VPMAlias.txt", "DMDDevice.ini");
  }

  @Override
  public List<String> getRootFolderInArchiveIndicators() {
    return Arrays.asList("VPMAlias.txt");
  }
}
