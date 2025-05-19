package de.mephisto.vpin.server.components.facades;

import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.connectors.github.GithubReleaseFactory;
import de.mephisto.vpin.server.system.SystemService;
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
public class VpxComponent implements ComponentFacade {

  @Autowired
  protected SystemService systemService;

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
    return systemService.resolveVpx64InstallFolder();
  }

  @Nullable
  @Override
  public Date getModificationDate() {
    File setupExe = systemService.resolveVpx64Exe();
    if (setupExe != null && setupExe.exists()) {
      return new Date(setupExe.lastModified());
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
    return Arrays.asList("VPinballX64.exe", "VPinballX.exe", "VPinballX_GL.exe");
  }
}
