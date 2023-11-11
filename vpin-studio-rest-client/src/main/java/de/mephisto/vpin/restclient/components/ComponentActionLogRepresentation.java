package de.mephisto.vpin.restclient.components;

import org.apache.commons.lang3.StringUtils;

public class ComponentActionLogRepresentation {
  private String logsSummary;
  private String diffSummary;

  private String status;
  private boolean simulated;
  private boolean diff;

  private String summary;

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
