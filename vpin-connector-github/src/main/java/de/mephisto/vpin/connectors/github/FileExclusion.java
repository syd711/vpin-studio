package de.mephisto.vpin.connectors.github;

import edu.umd.cs.findbugs.annotations.NonNull;

public class FileExclusion {
  private final String name;
  private final boolean backup;

  public FileExclusion(@NonNull String name, boolean backup) {
    this.name = name;
    this.backup = backup;
  }

  public String getName() {
    return name;
  }

  public boolean isBackup() {
    return backup;
  }
}
