package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.frontend.FrontendService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class TableAssetsService {
  private final static Logger LOG = LoggerFactory.getLogger(TableAssetsService.class);

  @Autowired
  private FrontendService frontendService;


  public List<TableAsset> search(@NonNull EmulatorType emulatorType, @NonNull VPinScreen screen, @NonNull String term) throws Exception {
    TableAssetsAdapter adapter = frontendService.getTableAssetAdapter();
    if (adapter != null) {
      return adapter.search(emulatorType.name(), screen.getSegment(), term);
    }
    else {
      return Collections.emptyList();
    }
  }

  public Optional<TableAsset> get(@NonNull EmulatorType emulatorType, @NonNull VPinScreen screen, @NonNull String folder, @NonNull String name) throws Exception {
    TableAssetsAdapter adapter = frontendService.getTableAssetAdapter();
    if (adapter != null) {
      return adapter.get(emulatorType.name(), screen.getSegment(), folder, name);
    }
    return Optional.empty();
  }

  public void download(@NonNull TableAsset asset, @NonNull File target) {
    TableAssetsAdapter adapter = frontendService.getTableAssetAdapter();
    if (adapter != null) {
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
      }
      catch (Exception e) {
        //do not log URL
        LOG.error("Failed to execute download: " + e.getClass().getSimpleName(), e);
      }
    }
  }

  public void download(OutputStream out, String url) {
    TableAssetsAdapter adapter = frontendService.getTableAssetAdapter();
    if (adapter != null) {
      try {
        adapter.writeAsset(out, url);
      }
      catch (Exception e) {
        //do not log URL
        LOG.error("Failed to execute download", e);
      }
    }
  }

  public boolean testConnection() {
    TableAssetsAdapter adapter = frontendService.getTableAssetAdapter();
    if (adapter != null) {
      return adapter.testConnection();
    }
    return false;
  }

  public boolean invalidateMediaCache() {
    TableAssetsAdapter adapter = frontendService.getTableAssetAdapter();
    if (adapter != null) {
      adapter.invalidateMediaCache();
      LOG.info("Invalidated media cache.");
    }
    return true;
  }
}
