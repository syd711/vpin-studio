package de.mephisto.vpin.restclient.components;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ComponentActionLogRepresentation {
  private String logsSummary;
  private String diffSummary;

  private String status;
  private boolean simulated;
  private boolean diff;

  private String summary;

  /** the compared files, only filled for comparisons */
  private List<ComponentDiffEntryRepresentation> diffEntries = new ArrayList<>();
  /** true if the comparison found a difference in one of the version relevant files */
  private boolean differing;

  public List<ComponentDiffEntryRepresentation> getDiffEntries() {
    return diffEntries;
  }

  public void setDiffEntries(List<ComponentDiffEntryRepresentation> diffEntries) {
    this.diffEntries = diffEntries;
  }

  public boolean isDiffering() {
    return differing;
  }

  public void setDiffering(boolean differing) {
    this.differing = differing;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getLogsSummary() {
    return logsSummary;
  }

  public void setLogsSummary(String logsSummary) {
    this.logsSummary = logsSummary;
  }

  public String getDiffSummary() {
    return diffSummary;
  }

  public void setDiffSummary(String diffSummary) {
    this.diffSummary = diffSummary;
  }

  public boolean isDiff() {
    return diff;
  }

  public void setDiff(boolean diff) {
    this.diff = diff;
  }

  public boolean isSimulated() {
    return simulated;
  }

  public void setSimulated(boolean simulated) {
    this.simulated = simulated;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (diff) {
      builder.append(diffSummary);
    }
    else {
      builder.append(logsSummary);
    }

    if (!StringUtils.isEmpty(summary)) {
      builder.append("\n\n");
      builder.append(summary);
    }
    return builder.toString();
  }
}
