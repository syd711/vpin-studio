package de.mephisto.vpin.restclient.components;

import java.util.ArrayList;
import java.util.List;

public class InstallLogRepresentation {
  private List<String> logs = new ArrayList<>();
  private String status;
  private boolean simulated;

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
