package de.mephisto.vpin.restclient.components;

/**
 * A single file of a component comparison: an installed file compared with the one of a release artifact.
 */
public class ComponentDiffEntryRepresentation {
  public static final String FILE_MATCH = "FILE_MATCH";
  public static final String SIZE_DIFF = "SIZE_DIFF";
  public static final String TARGET_FOLDER_NOT_EXIST = "TARGET_FOLDER_NOT_EXIST";
  public static final String TARGET_FILE_NOT_EXIST = "TARGET_FILE_NOT_EXIST";
  public static final String SKIPPED = "SKIPPED";

  private String file;
  private String state;
  /** size of the installed file, -1 if unknown */
  private long installedSize = -1;
  /** size of the file in the release artifact, -1 if unknown */
  private long releaseSize = -1;

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public long getInstalledSize() {
    return installedSize;
  }

  public void setInstalledSize(long installedSize) {
    this.installedSize = installedSize;
  }

  public long getReleaseSize() {
    return releaseSize;
  }

  public void setReleaseSize(long releaseSize) {
    this.releaseSize = releaseSize;
  }

  @Override
  public String toString() {
    return state + ": " + file;
  }
}
