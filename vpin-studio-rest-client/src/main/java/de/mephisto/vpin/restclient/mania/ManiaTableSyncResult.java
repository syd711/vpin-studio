package de.mephisto.vpin.restclient.mania;

import de.mephisto.vpin.connectors.mania.model.TableScore;

import java.util.ArrayList;
import java.util.List;

public class ManiaTableSyncResult {
  private String result = null;
  private List<TableScore> tableScores = new ArrayList<>();

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public List<TableScore> getTableScores() {
    return tableScores;
  }

  public void setTableScores(List<TableScore> tableScores) {
    this.tableScores = tableScores;
  }
}
