package de.mephisto.vpin.connectors.assets;

import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

public interface TableAssetsAdapter {

  TableAssetConf getTableAssetConf();

  List<TableAsset> search(String emulatorName, String screenSegment, String term) throws Exception;

  Optional<TableAsset> get(String emulatorName, String screenSegment, String folder, String name) throws Exception;

  /**
   * Download the asset and write it to the stream
   * @param outputStream the stream to write on
   * @param urlString The URL of the asset
   */
  void writeAsset(OutputStream outputStream, String urlString) throws Exception;

  /**
   * Test the connection to the remote search server
   * @return true if connection successfull 
   */
  boolean testConnection();

  /**
   * Invalidate the underlying cache if any. 
   * Do nothing by default, must be overwriden when cache is used.
   */
  default void invalidateMediaCache() {
  }
}
