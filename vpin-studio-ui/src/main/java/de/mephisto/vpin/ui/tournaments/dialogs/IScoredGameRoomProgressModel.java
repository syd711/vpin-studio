package de.mephisto.vpin.ui.tournaments.dialogs;

import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class IScoredGameRoomProgressModel extends ProgressModel<String> {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredGameRoomProgressModel.class);
  private final List<String> urls;
  private final Iterator<String> iterator;

  public IScoredGameRoomProgressModel(String dashboardUrl) {
    super("Loading iScored Game Room");
    this.urls = new ArrayList<>(Arrays.asList(dashboardUrl));
    this.iterator = this.urls.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public int getMax() {
    return urls.size();
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
  public String nextToString(String item) {
    return "Loading \"" + item + "\"";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, String dashboardUrl) {
    try {
      GameRoom gameRoom = IScored.loadGameRoom(dashboardUrl);
      progressResultModel.getResults().add(gameRoom);
    } catch (Exception e) {
      LOG.warn("Failed to load iscored dashboard: " + e.getMessage());
    }
  }
}
