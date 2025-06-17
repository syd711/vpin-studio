package de.mephisto.vpin.restclient.mania;

import de.mephisto.vpin.connectors.mania.model.TableScore;

import java.util.ArrayList;
import java.util.List;

public class ManiaTableSyncResult {
  private String result = null;
  private TableScore tableScore;

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
