package de.mephisto.vpin.ui.vps.containers;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;

public class VpsSelection {
  private VpsTable table;
  private VpsTableVersion version;

  public VpsSelection(VpsTable table, VpsTableVersion tableVersion) {
    this.table = table;
    version = tableVersion;
  }

  public void setTable(VpsTable table) {
    this.table = table;
  }

  public void setVersion(VpsTableVersion version) {
    this.version = version;
  }

  public VpsTable getTable() {
    return table;
  }

  public VpsTableVersion getVersion() {
    return version;
  }
}
