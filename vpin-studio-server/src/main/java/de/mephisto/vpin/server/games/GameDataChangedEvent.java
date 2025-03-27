package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.frontend.TableDetails;
import edu.umd.cs.findbugs.annotations.NonNull;

public class GameDataChangedEvent {
  private Game game;
  private TableDetails oldData;
  private TableDetails newData;

  public GameDataChangedEvent(@NonNull Game game, @NonNull TableDetails oldData, @NonNull TableDetails newData) {
    this.game = game;
    this.oldData = oldData;
    this.newData = newData;
  }

  public Game getGame() {
    return game;
  }

  public void setGame(Game game) {
    this.game = game;
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
