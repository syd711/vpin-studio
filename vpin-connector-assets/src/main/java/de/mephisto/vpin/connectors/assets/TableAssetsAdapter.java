package de.mephisto.vpin.connectors.assets;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

public interface TableAssetsAdapter {

  List<TableAsset> search(String screen, @NonNull String term);
}
