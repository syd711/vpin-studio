package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.mediasources.MediaSource;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.mediasources.MediaSourcesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class TableAssetsService {
  private final static Logger LOG = LoggerFactory.getLogger(TableAssetsService.class);

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private MediaSourcesService mediaSourcesService;

  public List<TableAsset> search(@NonNull EmulatorType emulatorType, @NonNull VPinScreen screen, int gameId, @NonNull String term) throws Exception {
    TableAssetsAdapter adapter = frontendService.getTableAssetAdapter();
    List<TableAsset> result = new ArrayList<>();
    if (adapter != null) {
      result = new ArrayList<>(adapter.search(emulatorType.name(), screen.getSegment(), term));
    }

    List<MediaSource> mediaSources = mediaSourcesService.getMediaSources();
    for (MediaSource mediaSource : mediaSources) {
      result.addAll(mediaSourcesService.search(mediaSource, emulatorType, screen, gameId, term));
    }

    return result;
  }

  public Optional<TableAsset> get(@NonNull EmulatorType emulatorType, @NonNull VPinScreen screen, int gameId, @NonNull String folder, @NonNull String name) throws Exception {
    TableAssetsAdapter adapter = frontendService.getTableAssetAdapter();
    if (adapter != null) {
      Optional<TableAsset> tableAsset = adapter.get(emulatorType.name(), screen.getSegment(), folder, name);
      if (tableAsset.isPresent()) {
        return tableAsset;
      }
    }

    List<MediaSource> mediaSources = mediaSourcesService.getMediaSources();
    for (MediaSource mediaSource : mediaSources) {
      Optional<TableAsset> tableAsset = mediaSourcesService.get(mediaSource, emulatorType, screen, gameId, folder, name);
      if (tableAsset.isPresent()) {
        return tableAsset;
      }
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

      String decoded = URLDecoder.decode(asset.getUrl(), StandardCharsets.UTF_8);
      String fileName = decoded.substring("file:///".length());
      File source = new File(fileName);
      if (source.exists()) {
        try {
          FileUtils.copyFile(source, target);
          LOG.info("Copied {} to {}", source.getAbsolutePath(), target.getAbsolutePath());
        }
        catch (Exception e) {
          //do not log URL
          LOG.error("Failed to execute media item copy: " + e.getClass().getSimpleName(), e);
        }
      }

    }
  }

  public void download(OutputStream out, String url) {
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
    if (adapter != null) {
      try {
        adapter.writeAsset(out, url);
      }
      catch (Exception e) {
        //do not log URL
        LOG.error("Failed to execute download: {}", e.getMessage(), e);
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
