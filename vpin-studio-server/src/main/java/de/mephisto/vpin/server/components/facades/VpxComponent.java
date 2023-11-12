package de.mephisto.vpin.server.components.facades;

import de.mephisto.githubloader.GithubRelease;
import de.mephisto.githubloader.GithubReleaseFactory;
import de.mephisto.githubloader.ReleaseArtifact;
import de.mephisto.githubloader.ReleaseArtifactActionLog;
import de.mephisto.vpin.server.games.GameEmulator;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class VpxComponent implements ComponentFacade {
  @NotNull
  @Override
  public String[] getDiffList() {
    return new String[]{".vbs", ".dll"};
  }

  @NotNull
  @Override
  public String getReleasesUrl() {
    return "https://github.com/vpinball/vpinball/releases";
  }

  @Override
  public GithubRelease loadRelease() throws IOException {
    return GithubReleaseFactory.loadRelease(getReleasesUrl(), Collections.emptyList(), Arrays.asList("Debug", "Source"));
  }

  @NotNull
  @Override
  public File getTargetFolder(@NotNull GameEmulator gameEmulator) {
    return gameEmulator.getTablesFolder().getParentFile();
  }

  @Nullable
  @Override
  public Date getModificationDate(@NonNull GameEmulator gameEmulator) {
    File setupExe = new File(gameEmulator.getTablesFolder().getParentFile(), "VPinballX64.exe");
    if (!setupExe.exists()) {
      setupExe = new File(gameEmulator.getTablesFolder().getParentFile(), "VPinballX.exe");
    }
    if (setupExe.exists()) {
      return new Date(setupExe.lastModified());
    }
    return null;
  }

  @Override
  public void postProcess(@NotNull GameEmulator gameEmulator, @NotNull ReleaseArtifact releaseArtifact, @NotNull ReleaseArtifactActionLog install) {
    try {
      install.log("\n\nExecuting Post Processing\n========================================");
      File vpxFile = new File(gameEmulator.getTablesFolder().getParentFile(), "VPinballX.exe");
      File vpxGlFile = new File(gameEmulator.getTablesFolder().getParentFile(), "VPinballX_GL64.exe");
      File vpx64File = new File(gameEmulator.getTablesFolder().getParentFile(), "VPinballX64.exe");

      if (vpxFile.exists()) {
        File backupFile = new File(vpxFile.getParentFile(), vpxFile.getName() + ".bak");
        if (backupFile.exists() && !backupFile.delete()) {
          install.setStatus("Failed to delete VPX backup file " + backupFile.getAbsolutePath());
          return;
        }

        if (!vpxFile.renameTo(backupFile)) {
          install.setStatus("Failed create VPX backup file " + backupFile.getAbsolutePath());
          return;
        }

        install.log("Created backup file " + backupFile.getAbsolutePath());
      }

      if (vpxGlFile.exists()) {
        FileUtils.copyFile(vpxGlFile, vpxFile);
        install.log("Copied " + vpxGlFile.getAbsolutePath() + " to " + vpxFile.getAbsolutePath());
      }

      if (vpx64File.exists()) {
        FileUtils.copyFile(vpx64File, vpxFile);
        install.log("Copied " + vpx64File.getAbsolutePath() + " to " + vpxFile.getAbsolutePath());
      }
    } catch (IOException e) {
      install.setStatus("Post processing failed: " + e.getMessage());
    }
  }

  @Nullable
  @Override
  public List<String> getExclusionList() {
    return Collections.emptyList();
  }

  @Override
  public boolean isSkipRootFolder() {
    return true;
  }
}
