package de.mephisto.vpin.connectors.github;

import edu.umd.cs.findbugs.annotations.NonNull;

public class DiffEntry {
  private final String file;
  private final DiffState state;
  private final long sourceSize;
  private final long targetSize;

  public DiffEntry(@NonNull String file, @NonNull DiffState diffState, long sourceSize, long targetSize) {
    this.file = file;
    this.state = diffState;
    this.sourceSize = sourceSize;
    this.targetSize = targetSize;
  }

  public String getFile() {
    return file;
  }

  public DiffState getState() {
    return state;
  }

  public long getSourceSize() {
    return sourceSize;
  }

  public long getTargetSize() {
    return targetSize;
  }

  @Override
  public String toString() {
    if (sourceSize > -1 | targetSize > -1) {
      return state + ": " + file + " (" + sourceSize + "/" + targetSize + ")";
    }
    return state + ": " + file;
  }
}
