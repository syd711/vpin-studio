package de.mephisto.vpin.server.components;

import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.connectors.github.ReleaseArtifact;
import de.mephisto.vpin.connectors.github.ReleaseArtifact.ArchiveSupplier;
import de.mephisto.vpin.connectors.github.ReleaseArtifactActionLog;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local cache of downloaded component release archives, so that a comparison, a simulation and the installation of
 * the same release artifact only download it once.
 * <p>
 * Layout: {@code resources/component-archives/<component>/<release tag>/<artifact name>}. Release assets of a tag do
 * not change, so a cached archive stays valid. Only the archives of the most recently used releases are kept.
 */
@Service
public class ComponentArchiveCache {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentArchiveCache.class);

  /**
   * Number of releases per component whose archives are kept.
   */
  static final int KEEP_RELEASES = 2;

  @Autowired
  private SystemService systemService;

  private final Map<String, Object> locks = new ConcurrentHashMap<>();

  /**
   * Returns a supplier for the archive of the given artifact, it downloads the archive only when it is not cached yet.
   */
  @NonNull
  public ArchiveSupplier supplier(@NonNull ComponentType type, @NonNull GithubRelease release, @NonNull ReleaseArtifact artifact) {
    return log -> get(type, release, artifact, log);
  }

  @NonNull
  public File get(@NonNull ComponentType type, @NonNull GithubRelease release, @NonNull ReleaseArtifact artifact, @NonNull ReleaseArtifactActionLog log) throws Exception {
    File archive = getArchiveFile(type, release.getTag(), artifact.getName());
    // concurrent requests for the same archive wait for a single download
    synchronized (locks.computeIfAbsent(archive.getAbsolutePath(), k -> new Object())) {
      File releaseFolder = archive.getParentFile();
      if (archive.isFile() && archive.length() > 0) {
        log.log("Using cached archive " + archive.getAbsolutePath() + " (" + FileUtils.byteCountToDisplaySize(archive.length()) + ")");
        LOG.info("Using cached archive {}", archive.getAbsolutePath());
        releaseFolder.setLastModified(System.currentTimeMillis());
        return archive;
      }

      File downloaded = artifact.download(archive, log);
      releaseFolder.setLastModified(System.currentTimeMillis());
      prune(type, releaseFolder);
      return downloaded;
    }
  }

  /**
   * Removes a cached archive, e.g. after it could not be read, so that the next action downloads it again.
   */
  public void invalidate(@NonNull ComponentType type, @NonNull GithubRelease release, @NonNull ReleaseArtifact artifact) {
    File archive = getArchiveFile(type, release.getTag(), artifact.getName());
    synchronized (locks.computeIfAbsent(archive.getAbsolutePath(), k -> new Object())) {
      if (archive.exists() && archive.delete()) {
        LOG.info("Removed cached archive {}", archive.getAbsolutePath());
      }
    }
  }

  @NonNull
  File getArchiveFile(@NonNull ComponentType type, @NonNull String tag, @NonNull String artifactName) {
    File releaseFolder = new File(systemService.getComponentArchiveFolder(type), toSafeName(tag));
    return new File(releaseFolder, toSafeName(artifactName));
  }

  /**
   * Deletes the archives of older releases and the files of the previous flat cache layout, which can not be
   * attributed to a release.
   */
  private void prune(@NonNull ComponentType type, @NonNull File current) {
    File[] files = systemService.getComponentArchiveFolder(type).listFiles();
    if (files == null) {
      return;
    }

    for (File file : files) {
      if (file.isFile()) {
        delete(file);
      }
    }

    List<File> releaseFolders = Arrays.stream(files)
        .filter(File::isDirectory)
        .filter(f -> !f.equals(current))
        .sorted(Comparator.comparingLong(File::lastModified).reversed())
        .toList();
    // the current release counts as one of the kept ones
    for (int i = KEEP_RELEASES - 1; i < releaseFolders.size(); i++) {
      delete(releaseFolders.get(i));
    }
  }

  private void delete(@NonNull File file) {
    try {
      FileUtils.forceDelete(file);
      LOG.info("Removed outdated component archive {}", file.getAbsolutePath());
    }
    catch (Exception e) {
      LOG.warn("Failed to remove outdated component archive {}: {}", file.getAbsolutePath(), e.getMessage());
    }
  }

  @NonNull
  static String toSafeName(@NonNull String name) {
    String safe = name.replaceAll("[^A-Za-z0-9._-]", "_");
    // no relative path segments
    return safe.replaceAll("^\\.+", "_");
  }
}
