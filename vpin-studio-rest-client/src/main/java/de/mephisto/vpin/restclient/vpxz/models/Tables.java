package de.mephisto.vpin.restclient.vpxz.models;

import java.util.ArrayList;
import java.util.List;

/**
 * http://192.168.178.177:2112/download?q=10.8%2Ftables.json
 */
public class Tables {
  private int tableCount = 0;
  private List<Table> tables = new ArrayList<>();

  public int getTableCount() {
    return tableCount;
  }

  public void setTableCount(int tableCount) {
    this.tableCount = tableCount;
  }

  public List<Table> getTables() {
    return tables;
  }

  public void setTables(List<Table> tables) {
    this.tables = tables;
  }
}
