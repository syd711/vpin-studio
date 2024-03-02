package de.mephisto.vpin.server.components.facades;

import de.mephisto.githubloader.GithubRelease;
import de.mephisto.githubloader.GithubReleaseFactory;
import de.mephisto.githubloader.ReleaseArtifact;
import de.mephisto.githubloader.ReleaseArtifactActionLog;
import de.mephisto.vpin.server.games.GameEmulator;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SerumComponent implements ComponentFacade {
  @NotNull
  @Override
  public String[] getDiffList() {
    return new String[]{".dll"};
  }

  @NotNull
  @Override
  public String getReleasesUrl() {
    return "https://github.com/zesinger/libserum/releases";
  }

  @Override
  public List<GithubRelease> loadReleases() throws IOException {
    return GithubReleaseFactory.loadReleases(getReleasesUrl(), Collections.emptyList(), Arrays.asList("Debug", "Source", "linux", "sc-", "macos", "android", "arm"));
  }

  @NotNull
  @Override
  public File getTargetFolder(@NotNull GameEmulator gameEmulator) {
    return gameEmulator.getMameFolder();
  }

  @Nullable
  @Override
  public Date getModificationDate(@NonNull GameEmulator gameEmulator) {
    File testExe = new File(gameEmulator.getMameFolder(), "serum_test.exe");
    if (testExe.exists()) {
      return new Date(testExe.lastModified());
    }
    return null;
  }

  @Override
  public void postProcess(@NotNull GameEmulator gameEmulator, @NotNull ReleaseArtifact releaseArtifact, @NotNull ReleaseArtifactActionLog install) {
//    try {
//      install.log("\n\nExecuting Post Processing\n========================================");
//      File vpxFile = new File(gameEmulator.getTablesFolder().getParentFile(), "VPinballX.exe");
//      File vpxGlFile = new File(gameEmulator.getTablesFolder().getParentFile(), "VPinballX_GL64.exe");
//      File vpx64File = new File(gameEmulator.getTablesFolder().getParentFile(), "VPinballX64.exe");

//      if (vpxFile.exists()) {
//        File backupFile = new File(vpxFile.getParentFile(), vpxFile.getName() + ".bak");
//        if (backupFile.exists() && !backupFile.delete()) {
//          install.setStatus("Failed to delete VPX backup file " + backupFile.getAbsolutePath());
//          return;
//        }
//
//        if (!vpxFile.renameTo(backupFile)) {
//          install.setStatus("Failed create VPX backup file " + backupFile.getAbsolutePath());
//          return;
//        }
//
//        install.log("Created backup file " + backupFile.getAbsolutePath());
//      }
//
//      if (vpxGlFile.exists()) {
//        FileUtils.copyFile(vpxGlFile, vpxFile);
//        install.log("Copied " + vpxGlFile.getAbsolutePath() + " to " + vpxFile.getAbsolutePath());
//      }
//
//      if (vpx64File.exists()) {
//        FileUtils.copyFile(vpx64File, vpxFile);
//        install.log("Copied " + vpx64File.getAbsolutePath() + " to " + vpxFile.getAbsolutePath());
//      }
//    } catch (IOException e) {
//      install.setStatus("Post processing failed: " + e.getMessage());
//    }
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
