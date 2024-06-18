package de.mephisto.vpin.connectors.assets;

import java.io.File;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface TableAssetsAdapter {

  List<TableAsset> search(@NonNull String emulatorName, @NonNull String screenSegment, @NonNull String term) throws Exception;

  public void download(@NonNull TableAsset asset, @NonNull File target) throws Exception;
}
