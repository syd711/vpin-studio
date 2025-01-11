package de.mephisto.vpin.server.components.facades;

import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.connectors.github.GithubReleaseFactory;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class VpxComponent implements ComponentFacade {
  @NonNull
  @Override
  public String[] getDiffList() {
    return new String[]{".vbs", ".dll", ".exe"};
  }

  @NonNull
  @Override
  public String getReleasesUrl() {
    return "https://github.com/vpinball/vpinball/releases";
  }

  @Override
  public List<GithubRelease> loadReleases() throws IOException {
    return GithubReleaseFactory.loadReleases(getReleasesUrl(), Collections.emptyList(), Arrays.asList("Debug", "Source", "linux", "sc-", "macos", "ios", "android"));
  }

  @NonNull
  @Override
  public File getTargetFolder() {
    SystemInfo si = new SystemInfo();
    return si.resolveVpx64InstallFolder();
  }

  @Nullable
  @Override
  public Date getModificationDate() {
    SystemInfo si = new SystemInfo();
    File setupExe = si.resolveVpx64Exe();
    if (setupExe != null && setupExe.exists()) {
      return new Date(setupExe.lastModified());
    }
    return null;
  }

  @Nullable
  @Override
  public List<String> getExcludedFilenames() {
    return Collections.emptyList();
  }

  @Override
  public List<String> getRootFolderInArchiveIndicators() {
    return Arrays.asList("VPinballX64.exe", "VPinballX.exe", "VPinballX_GL.exe");
  }
}
