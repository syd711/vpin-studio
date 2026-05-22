package de.mephisto.vpin.server.components.facades;

import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.connectors.github.ReleaseArtifact;
import de.mephisto.vpin.connectors.github.ReleaseArtifactActionLog;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

public interface ComponentFacade {
  @NonNull
  String[] getDiffList();

  @NonNull
  String getReleasesUrl();

  List<GithubRelease> loadReleases() throws IOException;

  @Nullable
  File getTargetFolder();

  @Nullable
  OffsetDateTime getModificationDate();

  @NonNull
  List<String> getExcludedFilenames();

  /**
   * Is stronger than the exclusions
   * @return
   */
  @NonNull
  default List<String> getIncludedFilenames() {
    return Collections.emptyList();
  }

  List<String> getRootFolderInArchiveIndicators();

  default boolean isInstalled() {
    return getModificationDate() != null;
  }

  /**
   * Executed after the installation of an update.
   * @param releaseArtifact
   * @param install
   */
  default void postProcess(@NonNull ReleaseArtifact releaseArtifact, @NonNull ReleaseArtifactActionLog install) {

  }

  default void preProcess(@NonNull ReleaseArtifact releaseArtifact, @NonNull ReleaseArtifactActionLog install) {

  }
}
