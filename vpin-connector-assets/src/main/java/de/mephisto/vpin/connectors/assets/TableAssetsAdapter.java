package de.mephisto.vpin.connectors.assets;

import java.io.OutputStream;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface TableAssetsAdapter {

  List<TableAsset> search(@NonNull String emulatorName, @NonNull String screenSegment, @NonNull String term) throws Exception;

  /**
   * Download the asset and write it to the stream 
   * @param outputStream The OutputStream where the asset should be writen to
   * @param url The URL of the asset
   */
  void writeAsset(OutputStream outputStream, String url) throws Exception;

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
