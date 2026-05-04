package de.mephisto.vpin.ui.preferences.dialogs;

import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class IScoredGameRoomDeletionProgressModel extends ProgressModel<IScoredGameRoom> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final List<IScoredGameRoom> gameRooms;
  private final Iterator<IScoredGameRoom> iterator;

  public IScoredGameRoomDeletionProgressModel(IScoredGameRoom gameRoom) {
    super("Deleting iScored Game Room");
    this.gameRooms = new ArrayList<>(Arrays.asList(gameRoom));
    this.iterator = this.gameRooms.iterator();
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
    return gameRooms.size();
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  @Override
  public IScoredGameRoom getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(IScoredGameRoom item) {
    return "Deleting \"" + item.getUrl() + "\"";
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    super.finalizeModel(progressResultModel);
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, IScoredGameRoom room) {
    try {
      client.getiScoredService().deleteGameRoom(room.getUuid());
    }
    catch (Exception e) {
      LOG.warn("Failed to delete iscored game room: " + e.getMessage());
    }
  }
}
