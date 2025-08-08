package de.mephisto.vpin.connectors.assets;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

public interface TableAssetsAdapter<T> {

  TableAssetSource getAssetSource();

  List<TableAsset> search(String emulatorName, String screenSegment, T game, String term) throws Exception;

  Optional<TableAsset> get(String emulatorName, String screenSegment, T game, String folder, String name) throws Exception;

  /**
   * Download the asset and write it to the stream
   *
   * @param outputStream the stream to write on
   * @param tableAsset   The asset
   */
  void writeAsset(@NonNull OutputStream outputStream, @NonNull TableAsset tableAsset) throws Exception;

  /**
   * Test the connection to the remote search server
   *
   * @return true if connection successfull
   */
  boolean testConnection();

  /**
   * Invalidate the underlying cache if any.
   * Do nothing by default, must be overwritten when cache is used.
   */
  default void invalidateMediaCache() {
  }
}
