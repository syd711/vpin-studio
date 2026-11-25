package de.mephisto.vpin.connectors.github;

/**
 * Receives the progress of a download or of the processing of a release archive.
 */
@FunctionalInterface
public interface ReleaseArtifactProgressListener {

  enum Phase {
    DOWNLOAD,
    COMPARE,
    SIMULATE,
    EXTRACT
  }

  /**
   * @param done  the bytes processed so far
   * @param total the total number of bytes, -1 when unknown
   */
  void onProgress(Phase phase, long done, long total);
}
