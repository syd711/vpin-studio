package de.mephisto.vpin.connectors.github;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ReleaseArtifact {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final GithubRelease githubRelease;

  private String name;
  private String url;

  public ReleaseArtifact(GithubRelease githubRelease) {
    this.githubRelease = githubRelease;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  public ReleaseArtifactActionLog diff(@NonNull File sourceArchive, @NonNull File targetFolder, @NonNull List<String> rootFileIndicators, @NonNull List<String> excludedFiles, @NonNull List<String> includedFiles,  @NonNull String... names) {
    long start = System.currentTimeMillis();
    ReleaseArtifactActionLog installLog = new ReleaseArtifactActionLog(false, true);
    try {
      if (!sourceArchive.exists()) {
        sourceArchive = new Downloader(this.getUrl(), installLog).download(sourceArchive);
      }

      if (!sourceArchive.exists()) {
        installLog.setStatus("Archive download failed for " + this + " failed, cancelling diff.");
        throw new UnsupportedOperationException("Archive download failed for " + this + " failed, cancelling diff.");
      }

      ArchiveHandler handler = new ArchiveHandler(sourceArchive, installLog, rootFileIndicators, excludedFiles, includedFiles);
      handler.diff(targetFolder);
      long duration = System.currentTimeMillis() - start;

      if (!installLog.hasDiffFor(names)) {
        StringBuilder summary = new StringBuilder();
        summary.append("-------------------------------------------------------------------------------------\n");
        summary.append("SUCCESS\n");
        summary.append("RESULT:\n");
        summary.append("The version tag \"" + githubRelease.getTag() + "\" of artifact \"" + this.name + "\" matches with the current installation.\n");
        summary.append("The following files have been checked for the version comparison: " + String.join(", ", names) + "\n");
        summary.append("-------------------------------------------------------------------------------------\n");
        summary.append("Total time:\t" + duration + "ms\n");
        summary.append("Finished at:\t" + DateFormat.getDateTimeInstance().format(new Date()) + "\n");
        summary.append("-------------------------------------------------------------------------------------\n");
        installLog.setSummary(summary.toString());
      }
      else {
        StringBuilder summary = new StringBuilder();
        summary.append("-------------------------------------------------------------------------------------\n");
        summary.append("SUCCESS\n");
        summary.append("RESULT:\n");
        summary.append("The artifact \"" + this.name + "\" does not match with the current installation, your installation may be outdated.\n");
        summary.append("The following files have been checked for the version comparison: " + String.join(", ", names) + "\n");
        summary.append("-------------------------------------------------------------------------------------\n");
        summary.append("Total time:\t" + duration + "ms\n");
        summary.append("Finished at:\t" + DateFormat.getDateTimeInstance().format(new Date()) + "\n");
        summary.append("-------------------------------------------------------------------------------------\n");
        installLog.setSummary(summary.toString());
      }

      LOG.info(installLog.toLogString());
      return installLog;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - start;
      StringBuilder summary = new StringBuilder();
      summary.append("-------------------------------------------------------------------------------------\n");
      summary.append("FAILED\n");
      summary.append("RESULT:\n");
      summary.append(e.getMessage());
      summary.append("-------------------------------------------------------------------------------------\n");
      summary.append("Total time:\t" + duration + "ms\n");
      summary.append("Finished at:\t" + DateFormat.getDateTimeInstance().format(new Date()) + "\n");
      summary.append("-------------------------------------------------------------------------------------\n");
      installLog.setSummary(summary.toString());
      installLog.setStatus(e.getMessage());
    }
    return installLog;
  }

  public ReleaseArtifactActionLog simulateInstall(@NonNull File targetFolder, @NonNull List<String> rootFileIndicators, @NonNull List<String> excludedFiles, @NonNull List<String> includedFiles) {
    return install(targetFolder, true, rootFileIndicators, excludedFiles, includedFiles);
  }

  public ReleaseArtifactActionLog install(@NonNull File targetFolder, @NonNull List<String> rootFileIndicators, @NonNull List<String> excludedFiles, @NonNull List<String> includedFiles) {
    return install(targetFolder, false, rootFileIndicators, excludedFiles, includedFiles);
  }

  private ReleaseArtifactActionLog install(@NonNull File targetFolder, boolean simulate, @NonNull List<String> rootFileIndicators, List<String> excludedFiles, List<String> includedFiles) {
    ReleaseArtifactActionLog installLog = new ReleaseArtifactActionLog(simulate, false);
    try {
      File archive = new Downloader(this.getUrl(), installLog).download(null);
      if (!archive.exists()) {
        installLog.setStatus("Archive download failed for " + this + " failed, cancelling installation.");
        throw new UnsupportedOperationException("Archive download failed for " + this + " failed, cancelling installation.");
      }

      ArchiveHandler handler = new ArchiveHandler(archive, installLog, rootFileIndicators, excludedFiles, includedFiles);
      if (simulate) {
        handler.simulate(targetFolder);
      }
      else {
        handler.unzip(targetFolder);
      }

      if (!archive.delete()) {
        installLog.setStatus("Failed to delete download artifact \"" + archive.getAbsolutePath() + "\"");
        throw new UnsupportedOperationException("Failed to delete download artifact \"" + archive.getAbsolutePath() + "\"");
      }
      else {
        installLog.log("Deleted downloaded artifact \"" + archive.getAbsolutePath() + "\"");
      }

      LOG.info(installLog.toLogString());

      return installLog;
    } catch (Exception e) {
      LOG.error("Failed to run install (simulated= " + simulate + "): " + e.getMessage(), e);
      installLog.setStatus(e.getMessage());
    }
    return installLog;
  }
}
