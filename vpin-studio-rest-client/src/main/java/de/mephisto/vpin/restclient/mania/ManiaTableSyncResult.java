package de.mephisto.vpin.restclient.mania;

import java.util.ArrayList;
import java.util.List;

public class ManiaTableSyncResult {
  private List<ManiaTableSync> results = new ArrayList<>();
  private String vpsTableId;
  private String error;

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getVpsTableId() {
    return vpsTableId;
  }

  public void setVpsTableId(String vpsTableId) {
    this.vpsTableId = vpsTableId;
  }

  public List<ManiaTableSync> getResults() {
    return results;
  }

  public void setResults(List<ManiaTableSync> results) {
    this.results = results;
  }
}
