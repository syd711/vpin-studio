package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.frontend.TableDetails;
import edu.umd.cs.findbugs.annotations.NonNull;

public class GameDataChangedEvent {
  private final int gameId;
  private TableDetails oldData;
  private TableDetails newData;

  public GameDataChangedEvent(@NonNull int gameId, @NonNull TableDetails oldData, @NonNull TableDetails newData) {
    this.gameId = gameId;
    this.oldData = oldData;
    this.newData = newData;
  }

  public int getGameId() {
    return gameId;
  }

  public TableDetails getOldData() {
    return oldData;
  }

  public void setOldData(TableDetails oldData) {
    this.oldData = oldData;
  }

  public TableDetails getNewData() {
    return newData;
  }

  public void setNewData(TableDetails newData) {
    this.newData = newData;
  }
}
