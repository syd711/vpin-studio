package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.games.descriptors.TableUploadDescriptor;

public class TableUploadResult {
  private int gameId;
  private TableUploadDescriptor uploadMode;

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public TableUploadDescriptor getUploadMode() {
    return uploadMode;
  }

  public void setUploadMode(TableUploadDescriptor uploadMode) {
    this.uploadMode = uploadMode;
  }
}
