package de.mephisto.vpin.restclient.components;

import java.util.ArrayList;
import java.util.List;

public class InstallLogRepresentation {
  private List<String> logs = new ArrayList<>();
  private String status;

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
