package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class MediaCacheProgressModel extends ProgressModel<String> {
  private final Iterator<String> iterator;
  private final List<String> assetSources;


  public MediaCacheProgressModel() {
    super("Rebuilding Search Index");
    assetSources = client.getMediaSourcesService().getAssetSources().stream().map(s -> s.getId()).collect(Collectors.toList());
    this.iterator = assetSources.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public String getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return assetSources.size() == 1;
  }

  @Override
  public String nextToString(String v) {
    return "Invalidating Asset Source \"" + v + "\"";
  }

  @Override
  public int getMax() {
    return assetSources.size();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, String assetSourceId) {
    client.getGameMediaService().invalidateMediaCache(assetSourceId);
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
