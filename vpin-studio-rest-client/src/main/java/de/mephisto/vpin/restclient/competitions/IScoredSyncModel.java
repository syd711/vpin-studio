package de.mephisto.vpin.restclient.competitions;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;

public class IScoredSyncModel {
  private boolean invalidate;
  private boolean manualSubscription = false;
  private IScoredGameRoom gameRoom;
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

  public IScoredGameRoom getGameRoom() {
    return gameRoom;
  }

  public void setGameRoom(IScoredGameRoom gameRoom) {
    this.gameRoom = gameRoom;
  }

  public IScoredGame getGame() {
    return game;
  }

  public void setGame(IScoredGame game) {
    this.game = game;
  }
}
