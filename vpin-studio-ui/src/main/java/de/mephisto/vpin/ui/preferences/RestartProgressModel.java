package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;

public class RestartProgressModel extends ProgressModel<String> {
  private final static Logger LOG = LoggerFactory.getLogger(RestartProgressModel.class);

  private final Iterator<String> syncIterator;

  public RestartProgressModel() {
    super("Waiting For Server");
    this.syncIterator = Arrays.asList("Waiting for VPin Studio Server to become available again...").iterator();
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
  public boolean hasNext() {
    return this.syncIterator.hasNext();
  }

  @Override
  public String getNext() {
    return syncIterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public String nextToString(String msg) {
    return msg;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, String msg) {
    try {
      Thread.sleep(5000);
      while (!progressResultModel.isCancelled()) {
        Thread.sleep(2000);
        String version = Studio.client.getSystemService().getVersion();
        if (version != null) {
          break;
        }
      }
    } catch (Exception e) {
      LOG.error("Error waiting for server: " + e.getMessage(), e);
    }
  }
}
