package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.frontend.FrontendService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class TableAssetsService {
  private final static Logger LOG = LoggerFactory.getLogger(TableAssetsService.class);

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private TableAssetAdapterFactory tableAssetAdapterFactory;

  private final List<TableAssetsAdapter> tableAssetsAdapters = new ArrayList<>();

  public List<TableAsset> search(@NonNull EmulatorType emulatorType, @NonNull VPinScreen screen, @NonNull String term) throws Exception {
    List<TableAsset> result = new ArrayList<>();
    getAllAdapters().stream().forEach(adapter -> {
      try {
        List<TableAsset> search = adapter.search(emulatorType.name(), screen.getSegment(), term);
        result.addAll(search);
      }
      catch (Exception e) {
        LOG.error("Asset search using {} failed: {}", adapter, e.getMessage(), e);
      }
    });
    return result;
  }

  public Optional<TableAsset> get(@NonNull EmulatorType emulatorType, @NonNull VPinScreen screen, @NonNull String folder, @NonNull String name) throws Exception {
    TableAssetsAdapter adapter = frontendService.getTableAssetAdapter();
    if (adapter != null) {
      Optional<TableAsset> tableAsset = adapter.get(emulatorType.name(), screen.getSegment(), folder, name);
      if (tableAsset.isPresent()) {
        return tableAsset;
      }
    }
//
//    List<MediaSource> mediaSources = mediaSourcesService.getMediaSources();
//    for (MediaSource mediaSource : mediaSources) {
//      Optional<TableAsset> tableAsset = mediaSourcesService.get(mediaSource, emulatorType, screen, gameId, folder, name);
//      if (tableAsset.isPresent()) {
//        return tableAsset;
//      }
//    }


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
    }
  }

  public void download(@NonNull OutputStream out, @NonNull TableAsset tableAsset) {
    String url = tableAsset.getUrl();
    if (url.startsWith("file")) {
      try {
        String urlString = URLDecoder.decode(url, StandardCharsets.UTF_8);
        String fileName = urlString.substring("file:///".length());
        fileName = fileName.replaceAll("@2x", "");
        File f = new File(fileName);
        IOUtils.copy(new FileInputStream(f), out);
        return;
      }
      catch (Exception e) {
        LOG.error("Failed to execute file download: {}", e.getMessage(), e);
      }
    }

    TableAssetsAdapter adapter = frontendService.getTableAssetAdapter();
//    if (adapter != null) {
//      try {
//        adapter.writeAsset(out, url);
//      }
//      catch (Exception e) {
//        //do not log URL
//        LOG.error("Failed to execute download: {}", e.getMessage(), e);
//      }
//    }
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
    tableAssetsAdapters.forEach(TableAssetsAdapter::invalidateMediaCache);
    return true;
  }

  public void invalidateMediaSources(List<TableAssetSource> tableAssetSources) {
    tableAssetsAdapters.clear();
    tableAssetSources.stream().forEach(source -> {
      tableAssetsAdapters.add(tableAssetAdapterFactory.createAdapter(source));
    });
  }

  private List<TableAssetsAdapter> getAllAdapters() {
    List<TableAssetsAdapter> result = new ArrayList<>();
    TableAssetsAdapter adapter = frontendService.getTableAssetAdapter();
    if (adapter != null) {
      result.add(adapter);
    }
    result.addAll(tableAssetsAdapters);
    return result;
  }
}
