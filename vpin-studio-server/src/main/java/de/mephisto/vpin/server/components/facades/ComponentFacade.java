package de.mephisto.vpin.server.components.facades;

import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.connectors.github.ReleaseArtifact;
import de.mephisto.vpin.connectors.github.ReleaseArtifactActionLog;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
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
  Date getModificationDate();

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
