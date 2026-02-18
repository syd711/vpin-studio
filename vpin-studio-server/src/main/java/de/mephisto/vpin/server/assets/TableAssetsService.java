package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.connectors.assets.AssetLookupStrategy;
import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.vps.VpsService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class TableAssetsService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private VpsService vpsService;

  @Autowired
  private TableAssetAdapterFactory tableAssetAdapterFactory;

  private final List<TableAssetsAdapter<Game>> tableAssetsAdapters = new ArrayList<>();

  private final ExecutorService executorService = Executors.newCachedThreadPool();

  public List<TableAsset> search(@Nullable TableAssetSource source,
                                 @NonNull EmulatorType emulatorType,
                                 @NonNull String screenSegment,
                                 @Nullable Game game,
                                 @NonNull String term) throws Exception {
    List<TableAsset> result = new ArrayList<>();
    List<Callable<List<TableAsset>>> tasks = new ArrayList<>();

    List<TableAssetsAdapter<Game>> applicableAdapters = getAllAdapters().stream()
        .filter(a -> a.getAssetSource().isEnabled())
        .filter(a -> a.getAssetSource().getLookupStrategy().equals(AssetLookupStrategy.autoDetect) || a.getAssetSource().supportsScreen(screenSegment))
        .collect(Collectors.toList());

    if (source != null) {
      Optional<TableAssetsAdapter<Game>> matchingSource = applicableAdapters.stream()
          .filter(a -> a.getAssetSource().isEnabled())
          .filter(a -> a.getAssetSource().getId().equals(source.getId()))
          .findFirst();
      applicableAdapters.clear();
      if (matchingSource.isPresent()) {
        applicableAdapters.add(matchingSource.get());
      }
    }

    applicableAdapters.forEach(adapter -> {
      tasks.add(() -> {
        try {
          return adapter.search(emulatorType.name(), screenSegment, game, term);
        }
        catch (Exception e) {
          LOG.error("Asset search using {} failed: {}", adapter, e.getMessage(), e);
        }
        return Collections.emptyList();
      });
    });

    List<Future<List<TableAsset>>> searches = executorService.invokeAll(tasks);
    for (Future<List<TableAsset>> search : searches) {
      List<TableAsset> tableAssets = search.get(15, TimeUnit.SECONDS);
      result.addAll(tableAssets);
    }
    return result.stream().filter(Objects::nonNull).collect(Collectors.toList());
  }

  public Optional<TableAsset> get(@Nullable TableAssetSource source,
                                  @NonNull EmulatorType emulatorType,
                                  @NonNull String screenSegment,
                                  @Nullable Game game,
                                  @NonNull String folder,
                                  @NonNull String name) throws Exception {
    Optional<TableAssetsAdapter<Game>> adapter = getAllAdapters().stream().filter(a -> source == null || a.getAssetSource().getId().equalsIgnoreCase(source.getId())).findFirst();
    if (adapter.isPresent()) {
      TableAssetsAdapter<Game> tableAssetsAdapter = adapter.get();
      return tableAssetsAdapter.get(emulatorType.name(), screenSegment, game, folder, name);
    }
    return Optional.empty();
  }

  public void download(@NonNull TableAsset asset, @NonNull File target) {
    String assetSourceId = asset.getSourceId();
    Optional<TableAssetsAdapter<Game>> adapter = getAllAdapters().stream().filter(a -> a.getAssetSource().getId().equalsIgnoreCase(assetSourceId)).findFirst();
    if (adapter.isPresent()) {
      if (target.exists()) {
        LOG.info("Asset \"{}\" already exists and will be replaced.", target.getName());
        if (!target.delete()) {
          LOG.error("Failed to delete existing asset {}", target.getAbsolutePath());
          return;
        }
      }
      try (FileOutputStream fileOutputStream = new FileOutputStream(target)) {
        adapter.get().writeAsset(fileOutputStream, asset, -1, -1);
        LOG.info("Downloaded file {}", target.getAbsolutePath());
      }
      catch (Exception e) {
        LOG.error("Failed to execute download: {}", e.getMessage(), e);
      }
    }
  }

  public void download(@NonNull OutputStream out, @NonNull TableAsset tableAsset, long start, long length) throws Exception {
    String assetSourceId = tableAsset.getSourceId();
    Optional<TableAssetsAdapter<Game>> adapter = getAllAdapters().stream().filter(a -> a.getAssetSource().getId().equalsIgnoreCase(assetSourceId)).findFirst();
    if (adapter.isPresent()) {
      adapter.get().writeAsset(out, tableAsset, start, length);
    }
  }

  public boolean testConnection(@NonNull String assetSourceId) {
    Optional<TableAssetsAdapter<Game>> adapter = getAllAdapters().stream().filter(a -> a.getAssetSource().getId().equalsIgnoreCase(assetSourceId)).findFirst();
    return adapter.map(TableAssetsAdapter::testConnection).orElse(false);
  }

  public boolean invalidateMediaCache(@NonNull String assetSourceId) {
    Optional<TableAssetsAdapter<Game>> adapter = getAllAdapters().stream().filter(a -> a.getAssetSource().getId().equalsIgnoreCase(assetSourceId)).findFirst();
    if (adapter.isPresent()) {
      adapter.get().invalidateMediaCache();
      LOG.info("Invalidated media cache.");
    }
    tableAssetsAdapters.forEach(TableAssetsAdapter::invalidateMediaCache);
    return true;
  }

  public void invalidateMediaSources(List<TableAssetSource> tableAssetSources) {
    tableAssetsAdapters.clear();
    tableAssetSources.forEach(source -> {
      tableAssetsAdapters.add(tableAssetAdapterFactory.createAdapter(vpsService, source));
    });
  }

  private List<TableAssetsAdapter<Game>> getAllAdapters() {
    List<TableAssetsAdapter<Game>> result = new ArrayList<>();
    TableAssetsAdapter<Game> adapter = frontendService.getTableAssetAdapter();
    if (adapter != null) {
      result.add(adapter);
    }
    result.addAll(tableAssetsAdapters);
    return result;
  }
}
