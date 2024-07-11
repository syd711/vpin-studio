package de.mephisto.vpin.connectors.assets;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

public interface TableAssetsAdapter {

  List<TableAsset> search(String emulatorName, String screenSegment, String term) throws Exception;

  Optional<TableAsset> get(String emulatorName, String screenSegment, String folder, String name) throws Exception;

  /**
   * Download the asset and write it to the stream
   * @param url The URL of the asset
   */
  InputStream readAsset(String url) throws Exception;

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
