package de.mephisto.vpin.restclient.mania;

import de.mephisto.vpin.connectors.mania.model.TableScore;

public class ManiaTableSyncResult {
  private String result = null;
  private TableScore tableScore;
  private boolean denied = false;

  public boolean isDenied() {
    return denied;
  }

  public void setDenied(boolean denied) {
    this.denied = denied;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public TableScore getTableScore() {
    return tableScore;
  }

  public void setTableScore(TableScore tableScore) {
    this.tableScore = tableScore;
  }
}
