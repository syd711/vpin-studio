package de.mephisto.vpin.connectors.github;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReleaseArtifactActionLog {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final List<String> logs = new ArrayList<>();
  private final List<DiffEntry> diffEntries = new ArrayList<>();
  private String status;
  private final boolean simulated;
  private final boolean diff;
  private boolean differing = false;
  private String summary;

  public ReleaseArtifactActionLog(boolean simulate, boolean diff) {
    this.simulated = simulate;
    this.diff = diff;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public boolean isDiffering() {
    return differing;
  }

  public boolean hasDiffFor(String... names) {
    for (DiffEntry diffEntry : diffEntries) {
      if (diffEntry.getState().equals(DiffState.TARGET_FOLDER_NOT_EXIST)) {
        LOG.info("The directory " + diffEntry.getFile() + " does not exist, cancelling diff test.");
        differing = true;
        return true;
      }

      if (matchesDiffList(diffEntry, names)) {
        if (diffEntry.getState().equals(DiffState.TARGET_FILE_NOT_EXIST)) {
          LOG.info("The file " + diffEntry.getFile() + " does exist, cancelling diff test.");
          differing = true;
          return true;
        }

        if (!diffEntry.getState().equals(DiffState.FILE_MATCH)) {
          LOG.info("The file " + diffEntry.getFile() + " does not match with the installed one, cancelling diff test.");
          differing = true;
          return true;
        }
      }
    }
    return differing;
  }

  private boolean matchesDiffList(DiffEntry diffEntry, String[] names) {
    for (String name : names) {
      String fileName = diffEntry.getFile();
      File file = new File(fileName);
      if ((file.getName().equals(name) || file.getName().endsWith(name))) {
        return true;
      }
    }
    return false;
  }

  public boolean isDiff() {
    return diff;
  }

  public void addDiffEntry(String absolutePath, DiffState targetFolderNotExist, long sourceSize, long targetSize) {
    DiffEntry entry = new DiffEntry(absolutePath, targetFolderNotExist, sourceSize, targetSize);
    this.diffEntries.add(entry);
  }

  public List<DiffEntry> getDiffEntries() {
    return diffEntries;
  }

  public boolean isSimulated() {
    return simulated;
  }

  public void log(String msg) {
    String format = DateFormat.getTimeInstance().format(new Date());
    msg = "[" + format + "] " + msg;
    logs.add(msg);
  }

  public List<String> getLogs() {
    return logs;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String toLogString() {
    StringBuilder builder = new StringBuilder();
    logs.stream().forEach(l -> builder.append(l + "\n"));

    if (status != null) {
      builder.append("\n\nStatus:");
      builder.append(status);
    }
    return builder.toString();
  }

  public String toDiffString() {
    StringBuilder builder = new StringBuilder();
    diffEntries.stream().forEach(l -> builder.append(l + "\n"));
    return builder.toString();
  }
}
