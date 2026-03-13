package de.mephisto.vpin.ui.tables.alx.dialogs;

import de.mephisto.vpin.restclient.games.descriptors.DeleteDescriptor;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class AlxDeleteStatsProgressModel extends ProgressModel<String> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Iterator<String> iterator;
  private final int emulatorId;
  private final boolean deleteTime;
  private final boolean deletePlays;
  private final boolean deleteScores;
  private List<String> deletions = new ArrayList<>();

  public AlxDeleteStatsProgressModel(String title, int emulatorId, boolean deleteTime, boolean deletePlays, boolean deleteScores) {
    super(title);
    this.emulatorId = emulatorId;
    this.deleteTime = deleteTime;
    this.deletePlays = deletePlays;
    this.deleteScores = deleteScores;
    this.deletions.add("");
    this.iterator = this.deletions.iterator();
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
  public String getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public String nextToString(String term) {
    return null;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, String term) {
    try {
      if (deleteTime) {
        client.getAlxService().deleteTimePlayedForEmulator(emulatorId);
      }
      if (deletePlays) {
        client.getAlxService().deleteNumberOfPlaysForEmulator(emulatorId);
      }
      if (deleteScores) {
        DeleteDescriptor descriptor = new DeleteDescriptor();
        descriptor.setDeleteHighscores(true);

      }
    }
    catch (Exception e) {
      LOG.error("ALX deletion failed: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
