package de.mephisto.vpin.ui.vps;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;

public class VpsSelection {
  private VpsTable table;
  private VpsTableVersion version;

  public VpsSelection(VpsTable table, VpsTableVersion tableVersion) {
    this.table = table;
    version = tableVersion;
  }

  public VpsTable getTable() {
    return table;
  }

  public VpsTableVersion getVersion() {
    return version;
  }
}
