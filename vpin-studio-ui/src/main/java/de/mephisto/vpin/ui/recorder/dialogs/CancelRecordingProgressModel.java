
package de.mephisto.vpin.ui.recorder.dialogs;

import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;

import static de.mephisto.vpin.ui.Studio.client;

public class CancelRecordingProgressModel extends ProgressModel<String> {
  private final static Logger LOG = LoggerFactory.getLogger(CancelRecordingProgressModel.class);

  private boolean next = true;

  public CancelRecordingProgressModel() {
    super("Cancelling Recording");
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
    return next;
  }

  @Override
  public String getNext() {
    return "";
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public String nextToString(String v) {
    return "Awaiting process termination...";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, String v) {
    try {
      next = false;
      int count = 10;
      while (client.getFrontendService().isFrontendRunning() && count > 0) {
        Thread.sleep(1000);
        count--;
      }
    }
    catch (Exception e) {
      LOG.error("Error during dismissal: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean isCancelable() {
    return false;
  }
}
