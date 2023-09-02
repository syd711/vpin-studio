package de.mephisto.vpin.connectors.assets;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TableAssetsService {
  private final static Logger LOG = LoggerFactory.getLogger(TableAssetsService.class);

  private List<TableAssetsAdapter> adapters = new ArrayList<>();

  private final static String DOWNLOAD_SUFFIX = ".assetDwnld";

  public List<TableAsset> search(@NonNull String screen, @NonNull String term) {
    List<TableAsset> assets = new ArrayList<>();
    for (TableAssetsAdapter adapter : adapters) {
      assets.addAll(adapter.search(screen, term));
    }
    return assets;
  }

  public void registerAdapter(@NonNull TableAssetsAdapter adapter) {
    this.adapters.add(adapter);
  }

  public void download(@NonNull TableAsset asset, @NonNull File target) {
    String downloadUrl = asset.getUrl();
    try {
      LOG.info("Downloading " + downloadUrl);
      URL url = new URL(downloadUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      BufferedInputStream in = new BufferedInputStream(url.openStream());
      File tmp = new File(target.getParentFile(), target.getName() + DOWNLOAD_SUFFIX);
      if (tmp.exists()) {
        tmp.delete();
      }

      FileOutputStream fileOutputStream = new FileOutputStream(tmp);
      byte dataBuffer[] = new byte[1024];
      int bytesRead;
      while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
        fileOutputStream.write(dataBuffer, 0, bytesRead);
      }
      in.close();
      fileOutputStream.close();
      if (target.exists() && !target.delete()) {
        LOG.error("Failed to delete existing asset " + target.getAbsolutePath());
      }

      if (!tmp.renameTo(target)) {
        LOG.error("Failed to rename " + tmp.getAbsolutePath());
      }
      LOG.info("Downloaded file " + target.getAbsolutePath());
    } catch (Exception e) {
      LOG.error("Failed to execute download: " + e.getMessage(), e);
    }
  }
}
