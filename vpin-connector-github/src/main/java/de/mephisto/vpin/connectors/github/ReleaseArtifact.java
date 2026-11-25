package de.mephisto.vpin.connectors.github;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
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

  /**
   * Provides the archive file of this artifact, e.g. from a local cache, downloading it when needed.
   */
  @FunctionalInterface
  public interface ArchiveSupplier {
    @NonNull
    File get(@NonNull ReleaseArtifactActionLog log) throws Exception;
  }

  /**
   * Downloads this artifact into the given file, an existing file is replaced.
   */
  @NonNull
  public File download(@NonNull File targetFile, @NonNull ReleaseArtifactActionLog log) throws Exception {
    return new Downloader(this.getUrl(), log).download(targetFile);
  }

  public ReleaseArtifactActionLog diff(@NonNull File sourceArchive, @NonNull File targetFolder, @NonNull List<String> rootFileIndicators, @NonNull List<String> excludedFiles, @NonNull List<String> includedFiles,  @NonNull String... names) {
    return diff(log -> sourceArchive.exists() ? sourceArchive : download(sourceArchive, log), null, targetFolder, rootFileIndicators, excludedFiles, includedFiles, names);
  }

  public ReleaseArtifactActionLog diff(@NonNull ArchiveSupplier archiveSupplier, @Nullable ReleaseArtifactProgressListener progressListener, @NonNull File targetFolder, @NonNull List<String> rootFileIndicators, @NonNull List<String> excludedFiles, @NonNull List<String> includedFiles,  @NonNull String... names) {
    long start = System.currentTimeMillis();
    ReleaseArtifactActionLog installLog = new ReleaseArtifactActionLog(false, true, progressListener);
    try {
      File sourceArchive = archiveSupplier.get(installLog);
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
        summary.append("Finished at:\t" + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(OffsetDateTime.now()) + "\n");
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
        summary.append("Finished at:\t" + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(OffsetDateTime.now()) + "\n");
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
      summary.append("Finished at:\t" + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(OffsetDateTime.now()) + "\n");
      summary.append("-------------------------------------------------------------------------------------\n");
      installLog.setSummary(summary.toString());
      installLog.setStatus(e.getMessage());
    }
    return installLog;
  }

  /**
   * Downloads the artifact into a temporary file and simulates the installation.
   */
  public ReleaseArtifactActionLog simulateInstall(@NonNull File targetFolder, @NonNull List<String> rootFileIndicators, @NonNull List<String> excludedFiles, @NonNull List<String> includedFiles) {
    return install(this::downloadTemp, null, true, true, targetFolder, rootFileIndicators, excludedFiles, includedFiles);
  }

  /**
   * Downloads the artifact into a temporary file and installs it.
   */
  public ReleaseArtifactActionLog install(@NonNull File targetFolder, @NonNull List<String> rootFileIndicators, @NonNull List<String> excludedFiles, @NonNull List<String> includedFiles) {
    return install(this::downloadTemp, null, true, false, targetFolder, rootFileIndicators, excludedFiles, includedFiles);
  }

  /**
   * Simulates the installation of the supplied archive, the archive is kept.
   */
  public ReleaseArtifactActionLog simulateInstall(@NonNull ArchiveSupplier archiveSupplier, @Nullable ReleaseArtifactProgressListener progressListener, @NonNull File targetFolder, @NonNull List<String> rootFileIndicators, @NonNull List<String> excludedFiles, @NonNull List<String> includedFiles) {
    return install(archiveSupplier, progressListener, false, true, targetFolder, rootFileIndicators, excludedFiles, includedFiles);
  }

  /**
   * Installs the supplied archive, the archive is kept.
   */
  public ReleaseArtifactActionLog install(@NonNull ArchiveSupplier archiveSupplier, @Nullable ReleaseArtifactProgressListener progressListener, @NonNull File targetFolder, @NonNull List<String> rootFileIndicators, @NonNull List<String> excludedFiles, @NonNull List<String> includedFiles) {
    return install(archiveSupplier, progressListener, false, false, targetFolder, rootFileIndicators, excludedFiles, includedFiles);
  }

  @NonNull
  private File downloadTemp(@NonNull ReleaseArtifactActionLog log) throws Exception {
    return new Downloader(this.getUrl(), log).download(null);
  }

  private ReleaseArtifactActionLog install(@NonNull ArchiveSupplier archiveSupplier, @Nullable ReleaseArtifactProgressListener progressListener, boolean deleteArchive, boolean simulate, @NonNull File targetFolder,
                                           @NonNull List<String> rootFileIndicators, List<String> excludedFiles, List<String> includedFiles) {
    ReleaseArtifactActionLog installLog = new ReleaseArtifactActionLog(simulate, false, progressListener);
    try {
      File archive = archiveSupplier.get(installLog);
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

      if (deleteArchive) {
        if (!archive.delete()) {
          installLog.setStatus("Failed to delete download artifact \"" + archive.getAbsolutePath() + "\"");
          throw new UnsupportedOperationException("Failed to delete download artifact \"" + archive.getAbsolutePath() + "\"");
        }
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
