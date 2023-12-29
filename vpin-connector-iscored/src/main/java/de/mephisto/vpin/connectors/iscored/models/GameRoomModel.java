package de.mephisto.vpin.connectors.iscored.models;

import de.mephisto.vpin.connectors.iscored.Settings;

public class GameRoomModel {

  private int roomID;

  private Settings settings;

  public int getRoomID() {
    return roomID;
  }

  public void setRoomID(int roomID) {
    this.roomID = roomID;
  }

  public Settings getSettings() {
    return settings;
  }

  public void setSettings(Settings settings) {
    this.settings = settings;
  }
}
