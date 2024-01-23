package de.mephisto.vpin.connectors.vps;

import java.util.List;

public interface VpsSheetChangedListener {
  void vpsSheetChanged(List<VpsDiffer> diff);
}
