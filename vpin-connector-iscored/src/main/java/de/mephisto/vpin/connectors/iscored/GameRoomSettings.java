package de.mephisto.vpin.connectors.iscored;

import com.fasterxml.jackson.annotation.JsonFormat;

public class GameRoomSettings {
  private String roomName;

  private String gameOrder;

  private String publicScoresEnabled;

  private String adminApproval;

  public String getRoomName() {
    return roomName;
  }

  public void setRoomName(String roomName) {
    this.roomName = roomName;
  }

  public String getGameOrder() {
    return gameOrder;
  }

  public void setGameOrder(String gameOrder) {
    this.gameOrder = gameOrder;
  }

  public String getPublicScoresEnabled() {
    return publicScoresEnabled;
  }

  public void setPublicScoresEnabled(String publicScoresEnabled) {
    this.publicScoresEnabled = publicScoresEnabled;
  }

  public String getAdminApproval() {
    return adminApproval;
  }

  public void setAdminApproval(String adminApproval) {
    this.adminApproval = adminApproval;
  }
}
