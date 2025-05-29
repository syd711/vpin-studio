package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class CacheInvalidationProgressModel extends ProgressModel<String> {
  private final static Logger LOG = LoggerFactory.getLogger(CacheInvalidationProgressModel.class);
  private final boolean invalidateMame;
  private List<String> msgs;

  private final Iterator<String> iterator;

  public CacheInvalidationProgressModel(boolean invalidateMame) {
    super("Reloading Games");
    this.invalidateMame = invalidateMame;
    this.msgs = Arrays.asList("game");
    this.iterator = msgs.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return 1;
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  @Override
  public String getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(String msg) {
    return null;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, String msg) {
    try {
      client.getPinVolService().clearCache();
      client.getFrontendService().reload();
      client.getGameService().reload();

      if (invalidateMame) {
        client.getMameService().clearCache();
      }
//      client.getPupPackService().clearCache();
      client.getDmdService().clearCache();
      client.getSystemService().clearCache();
    }
    catch (Exception e) {
      LOG.error("Error invalidating caches: " + e.getMessage(), e);
    }
  }
}
