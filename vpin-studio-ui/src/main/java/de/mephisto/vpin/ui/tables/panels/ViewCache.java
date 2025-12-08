package de.mephisto.vpin.ui.tables.panels;

import de.mephisto.vpin.ui.tables.GameRepresentationModel;
import de.mephisto.vpin.ui.vps.VpsTablesController;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.scene.Node;

import java.util.HashMap;
import java.util.Map;

public class ViewCache<T, M extends BaseLoadingModel<T, M>> {

  private final Map<String, ViewCacheEntry> cache = new HashMap<>();

  @Nullable
  public <M extends BaseLoadingModel<T, M>> Node getCachedComponent(@NonNull String cacheKey, @NonNull M model) {
    String key = getKey(model);
    if (cache.containsKey(key)) {
      ViewCacheEntry viewCacheEntry = cache.get(key);
      if (viewCacheEntry.contains(cacheKey)) {
        return viewCacheEntry.getNode(cacheKey);
      }
    }
    return null;
  }

  public <M extends BaseLoadingModel<T, M>> void cacheComponent(@NonNull String cacheKey, @NonNull M model, @NonNull Node node) {
    String key = getKey(model);
    cache.put(key, new ViewCacheEntry(cacheKey, node));
//    System.out.println(cache.size());
  }

  public void clear(M model) {
    String key = getKey(model);
    cache.remove(key);
  }

  private <M extends BaseLoadingModel<T, M>> String getKey(M model) {
//    T bean = model.getBean();
//    if(bean instanceof GameRepresentationModel) {
//      return String.valueOf(((GameRepresentationModel)bean).getGameId());
//    }
//    if(bean instanceof VpsTablesController.VpsTableModel) {
//      return String.valueOf(((VpsTablesController.VpsTableModel)bean).getVpsTableId());
//    }
    return String.valueOf(model.getBean().hashCode());
  }

  public void clear() {
    this.cache.clear();
  }


  static class ViewCacheEntry {

    private final Map<String, Node> cache = new HashMap<>();

    ViewCacheEntry(String key, Node node) {
      cache.put(key, node);
    }

    public boolean contains(@NonNull String cacheKey) {
      return cache.containsKey(cacheKey);
    }

    public Node getNode(@NonNull String cacheKey) {
      return cache.get(cacheKey);
    }
  }
}
