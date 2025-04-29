package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class IScoredGameRoomSynchronizationProgressModel extends ProgressModel<IScoredGameRoom> {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredGameRoomSynchronizationProgressModel.class);

  private final Iterator<IScoredGameRoom> iterator;
  private final List<IScoredGameRoom> gameRooms;

  public IScoredGameRoomSynchronizationProgressModel(@NonNull List<IScoredGameRoom> gameRooms) {
    super("iScored Synchronization");
    this.gameRooms = gameRooms;
    this.iterator = this.gameRooms.iterator();
  }

  @Override
  public IScoredGameRoom getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public String nextToString(IScoredGameRoom c) {
    return "Synchronizing \"" + c.getUrl() + "\"";
  }

  @Override
  public int getMax() {
    return gameRooms.size();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, IScoredGameRoom next) {
    try {
      client.getCompetitionService().synchronizeIScoredGameRoom(next);
    }
    catch (Exception e) {
      LOG.error("Failed to sync competitions data: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "iScored Synchronization Failed", "Result: " + e.getMessage());
      });
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
