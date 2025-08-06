package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CacheTableAssetsAdapter implements TableAssetsAdapter {

  private static final int CACHE_SIZE = 300;

  private final TableAssetsAdapter delegate;

  private final List<TableAssetCacheResult> cache = new ArrayList<>();

  public CacheTableAssetsAdapter(TableAssetsAdapter delegate) {
    this.delegate = delegate;
  }

  @Override
  public TableAssetSource getAssetSource() {
    return delegate.getAssetSource();
  }

  @Override
  public Optional<TableAsset> get(String emulatorName, String screenSegment, String folder, String name) throws Exception {
    return delegate.get(emulatorName, screenSegment, folder, name);
  }

  private synchronized TableAssetCacheResult getCached(String screenSegment, String term) {
    for (TableAssetCacheResult s : this.cache) {
      if (s.term.equals(term) && s.screen.equals(screenSegment)) {
        return s;
      }
    }

    if (!StringUtils.isEmpty(term) && term.trim().contains(" ")) {
      term = term.split(" ")[0];
      for (TableAssetCacheResult s : this.cache) {
        if (s.term.equals(term) && s.screen.equals(screenSegment)) {
          return s;
        }
      }
    }

    return null;
  }

  @Override
  public List<TableAsset> search(String emulatorName, String screenSegment, String term) throws Exception {

    TableAssetCacheResult cached = getCached(screenSegment, term);
    if (cached != null) {
      return cached.result;
    }

    cached = new TableAssetCacheResult();
    cached.term = term;
    cached.screen = screenSegment;
    cached.result = delegate.search(emulatorName, screenSegment, term);

    cache.add(cached);
    if (cache.size() > CACHE_SIZE) {
      cache.remove(0);
    }

    return cached.result;
  }

  @Override
  public void writeAsset(@NonNull OutputStream out, @NonNull TableAsset tableAsset) throws Exception {
    delegate.writeAsset(out, tableAsset);
  }

  @Override
  public boolean testConnection() {
    return delegate.testConnection();
  }

  @Override
  public void invalidateMediaCache() {
    delegate.invalidateMediaCache();
    this.cache.clear();
  }


  private class TableAssetCacheResult {
    private String screen;
    private String term;
    private List<TableAsset> result;
  }
} 