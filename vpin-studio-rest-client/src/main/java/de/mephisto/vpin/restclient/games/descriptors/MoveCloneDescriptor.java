package de.mephisto.vpin.restclient.games.descriptors;

public class MoveCloneDescriptor {
  private int gameId;
  private int targetEmulatorId;
  private boolean move;

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public int getTargetEmulatorId() {
    return targetEmulatorId;
  }

  public void setTargetEmulatorId(int targetEmulatorId) {
    this.targetEmulatorId = targetEmulatorId;
  }

  public boolean isMove() {
    return move;
  }

  public void setMove(boolean move) {
    this.move = move;
  }
}
