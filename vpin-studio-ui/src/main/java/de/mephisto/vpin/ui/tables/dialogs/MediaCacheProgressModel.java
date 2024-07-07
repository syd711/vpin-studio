package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;

import static de.mephisto.vpin.ui.Studio.client;

public class MediaCacheProgressModel extends ProgressModel<String> {
  private final Iterator<String> iterator;

  public MediaCacheProgressModel() {
    super("Rebuilding Search Index");
    this.iterator = Arrays.asList("Waiting for index completion...").iterator();
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
    return true;
  }

  @Override
  public String nextToString(String v) {
    return v;
  }

  @Override
  public int getMax() {
    return 1;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, String tableAsset) {
    client.getGameMediaService().invalidateMediaCache();
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
