package de.mephisto.vpin.restclient.competitions;

import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;

public class IScoredSyncModel {
  private boolean invalidate;
  private IScoredGameRoom iScoredGameRoom;
  private IScoredGame game;

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
