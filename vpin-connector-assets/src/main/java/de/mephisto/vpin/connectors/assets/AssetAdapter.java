package de.mephisto.vpin.connectors.assets;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

public interface AssetAdapter {

  List<Asset> search(@NonNull String term);
}
