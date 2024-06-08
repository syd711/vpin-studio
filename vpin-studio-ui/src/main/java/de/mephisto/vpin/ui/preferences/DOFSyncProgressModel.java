package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;

public class DOFSyncProgressModel extends ProgressModel<String> {
  private final static Logger LOG = LoggerFactory.getLogger(DOFSyncProgressModel.class);

  private final Iterator<String> syncIterator;

  public DOFSyncProgressModel() {
    super("Synchronizing DOF Configuration");
    this.syncIterator = Arrays.asList("Downloading DOF configuration, please be patient...").iterator();
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
      JobExecutionResult sync = Studio.client.getDofService().sync(true);
      progressResultModel.getResults().add(sync);
    } catch (Exception e) {
      progressResultModel.getResults().add("Error synchronizing DOF: " + e.getMessage());
      LOG.error("Error synchronizing DOF: " + e.getMessage(), e);
    }
  }
}
