package de.mephisto.vpin.connectors.assets;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;

public class TableAssetsService {
  private final static Logger LOG = LoggerFactory.getLogger(TableAssetsService.class);

  private TableAssetsAdapter adapter = null;

  public List<TableAsset> search(@NonNull String emulatorName, @NonNull String screen, @NonNull String term) throws Exception {
    return adapter.search(emulatorName, screen, term);
  }

  public void registerAdapter(@NonNull TableAssetsAdapter adapter) {
    this.adapter = adapter;
  }

  public void download(@NonNull TableAsset asset, @NonNull File target) throws Exception {
    adapter.download(asset, target);
  }
}
