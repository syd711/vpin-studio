package de.mephisto.vpin.server.components.facades;

import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.connectors.github.GithubReleaseFactory;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.server.components.Component;
import de.mephisto.vpin.server.components.ComponentRepository;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.lang3.StringUtils;
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
public class VpxComponent implements ComponentFacade {

  @Autowired
  protected SystemService systemService;

  @Autowired
  private ComponentRepository componentRepository;

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
    File override = resolveOverrideFolder();
    return override != null ? override : systemService.resolveVpx64InstallFolder();
  }

  @Nullable
  @Override
  public OffsetDateTime getModificationDate() {
    File override = resolveOverrideFolder();
    if (override != null) {
      File exe = new File(override, "VPinballX64.exe");
      if (!exe.exists()) {
        exe = new File(override, "VPinballX.exe");
      }
      return exe.exists() ? OffsetDateTime.ofInstant(Instant.ofEpochMilli(exe.lastModified()), ZoneId.systemDefault()) : null;
    }

    File setupExe = systemService.resolveVpx64Exe();
    if (setupExe != null && setupExe.exists()) {
      return OffsetDateTime.ofInstant(Instant.ofEpochMilli(setupExe.lastModified()), ZoneId.systemDefault());
    }
    return null;
  }

  /**
   * A user-configured override (System Manager > Visual Pinball > target folder) always wins over the
   * Windows file-association-based auto-detection, which finds nothing for a portable or custom install.
   */
  @Nullable
  private File resolveOverrideFolder() {
    return componentRepository.findByType(ComponentType.vpinball)
        .map(Component::getTargetFolder)
        .filter(dir -> !StringUtils.isEmpty(dir))
        .map(File::new)
        .filter(File::exists)
        .orElse(null);
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
