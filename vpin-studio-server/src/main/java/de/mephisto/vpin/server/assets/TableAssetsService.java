package de.mephisto.vpin.server.assets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import edu.umd.cs.findbugs.annotations.NonNull;

@Service
public class TableAssetsService {
  private final static Logger LOG = LoggerFactory.getLogger(TableAssetsService.class);

  private TableAssetsAdapter adapter = null;

  public void registerAdapter(@NonNull TableAssetsAdapter adapter) {
    this.adapter = adapter;
  }


  public List<TableAsset> search(@NonNull EmulatorType emulatorType, @NonNull VPinScreen screen, @NonNull String term) throws Exception {
    if (adapter!=null) {
      return adapter.search(emulatorType.name(), screen.getSegment(), term);
    } else {
      return Collections.emptyList();
    }
  }

  public void download(@NonNull TableAsset asset, @NonNull File target) {
    if (adapter!=null) {
      if (target.exists()) {
        LOG.info("Asset " + target.getName() + " already exists and will be replaced.");
        if (!target.delete()) {
          LOG.error("Failed to delete existing asset " + target.getAbsolutePath());
          return;
        }
      }
      try (FileOutputStream fileOutputStream = new FileOutputStream(target)) {
        String downloadUrl = asset.getUrl();
        adapter.writeAsset(fileOutputStream, downloadUrl);
        LOG.info("Downloaded file " + target.getAbsolutePath());
      } catch (Exception e) {
        //do not log URL
        LOG.error("Failed to execute download: " + e.getClass().getSimpleName(), e);
      }
    }
  }

  public void download(String url, OutputStream outputStream) throws Exception {
    if (adapter!=null) {
      adapter.writeAsset(outputStream, url);
    }
  }

  public boolean testConnection() {
    if (adapter!=null) {
      return adapter.testConnection();
    }
    return false;
  }

  public boolean invalidateMediaCache() {
    if (adapter!=null) {
      adapter.invalidateMediaCache();
      LOG.info("Invalidated media cache.");
    }
    return true;
  }
}
