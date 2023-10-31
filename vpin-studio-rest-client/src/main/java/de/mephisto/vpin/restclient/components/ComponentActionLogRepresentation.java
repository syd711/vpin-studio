package de.mephisto.vpin.restclient.components;

import java.util.ArrayList;
import java.util.List;

public class ComponentActionLogRepresentation {
  private List<String> logs = new ArrayList<>();
  private String status;
  private boolean simulated;
  private boolean diff;

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

  public List<String> getLogs() {
    return logs;
  }

  public void setLogs(List<String> logs) {
    this.logs = logs;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
