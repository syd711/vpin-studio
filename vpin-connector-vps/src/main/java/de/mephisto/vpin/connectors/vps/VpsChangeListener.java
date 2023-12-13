package de.mephisto.vpin.connectors.vps;

import de.mephisto.vpin.connectors.vps.model.VpsTableDiff;

import java.util.List;

public interface VpsChangeListener {
  void vpsSheetChanged(List<VpsTableDiff> diff);
}
