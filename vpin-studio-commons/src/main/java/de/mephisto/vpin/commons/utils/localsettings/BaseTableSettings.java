package de.mephisto.vpin.commons.utils.localsettings;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseTableSettings extends LocalJsonSettings {

  private Map<String, Double> columnWith = new HashMap<>();

  private List<String> columnOrder = new ArrayList<>();

  public List<String> getColumnOrder() {
    return columnOrder;
  }

  public void setColumnOrder(List<String> columnOrder) {
    this.columnOrder = columnOrder;
  }

  public Map<String, Double> getColumnWith() {
    return columnWith;
  }

  public void setColumnWith(Map<String, Double> columnWith) {
    this.columnWith = columnWith;
  }

  public double getColumnWidth(@NonNull String key) {
    if (columnWith.containsKey(key)) {
      return columnWith.get(key);
    }
    return 0;
  }
}
