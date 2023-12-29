package de.mephisto.vpin.connectors.iscored;

public class GameRoom {

  private int roomID;
  private GameRoomSettings settings;

  public int getRoomID() {
    return roomID;
  }

  public void setRoomID(int roomID) {
    this.roomID = roomID;
  }

  public GameRoomSettings getSettings() {
    return settings;
  }

  public void setSettings(GameRoomSettings settings) {
    this.settings = settings;
  }
}
