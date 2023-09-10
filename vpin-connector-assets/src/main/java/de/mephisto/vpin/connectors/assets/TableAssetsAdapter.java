package de.mephisto.vpin.connectors.assets;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

public interface TableAssetsAdapter {

  List<TableAsset> search(@NonNull String key, @NonNull String screen, @NonNull String term) throws Exception;

  String decrypt(String key, String url);
}
