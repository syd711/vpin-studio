package de.mephisto.vpin.connectors.assets;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class TableAssetsService {
  private final static Logger LOG = LoggerFactory.getLogger(TableAssetsService.class);

  private TableAssetsAdapter adapter = null;

  public List<TableAsset> search(@NonNull String key, @NonNull String screen, @NonNull String term) throws Exception {
    return adapter.search(key, screen, term);
  }

  public void registerAdapter(@NonNull TableAssetsAdapter adapter) {
    this.adapter = adapter;
  }

  public void download(@NonNull TableAsset asset, @NonNull File target) {
    String downloadUrl = asset.getUrl();
    try {
      String urlString = adapter.decrypt(EncryptDecrypt.KEY, downloadUrl);
      urlString = urlString.replaceAll(" ", "%20");
      URL url = new URL(urlString);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      BufferedInputStream in = new BufferedInputStream(url.openStream());
      if (target.exists()) {
        LOG.info("Asset " + target.getName() + " already exists and will be replaced.");
        if (!target.delete()) {
          LOG.error("Failed to delete existing asset " + target.getAbsolutePath());
          return;
        }
      }

      FileOutputStream fileOutputStream = new FileOutputStream(target);
      byte dataBuffer[] = new byte[1024];
      int bytesRead;
      while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
        fileOutputStream.write(dataBuffer, 0, bytesRead);
      }
      in.close();
      fileOutputStream.close();
      LOG.info("Downloaded file " + target.getAbsolutePath());
    } catch (Exception e) {
      //do not log URL
      LOG.error("Failed to execute download: " + e.getClass().getSimpleName(), e);
    }
  }
}
