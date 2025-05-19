package de.mephisto.vpin.restclient.competitions;

import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;

import java.util.List;

public class IScoredSyncModel {
  private boolean invalidate;
  private boolean manualSubscription = false;
  private IScoredGameRoom iScoredGameRoom;
  private IScoredGame game;

  public boolean isManualSubscription() {
    return manualSubscription;
  }

  public void setManualSubscription(boolean manualSubscription) {
    this.manualSubscription = manualSubscription;
  }

  public boolean isInvalidate() {
    return invalidate;
  }

  public void setInvalidate(boolean invalidate) {
    this.invalidate = invalidate;
  }

  public IScoredGameRoom getiScoredGameRoom() {
    return iScoredGameRoom;
  }

  public void setiScoredGameRoom(IScoredGameRoom iScoredGameRoom) {
    this.iScoredGameRoom = iScoredGameRoom;
  }

  public IScoredGame getGame() {
    return game;
  }

  public void setGame(IScoredGame game) {
    this.game = game;
  }
}
