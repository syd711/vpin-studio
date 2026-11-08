package de.mephisto.vpin.restclient.wovp;

import de.mephisto.vpin.connectors.wovp.patcher.WovpPatchStatus;

/**
 * Result of applying or removing the WoVP telemetry patch of a table.
 */
public class WovpPatchResult {
  private int gameId;
  private WovpPatchStatus status;
  private String message;
  private String backupFile;

  public WovpPatchResult() {
  }

  public WovpPatchResult(int gameId, WovpPatchStatus status) {
    this.gameId = gameId;
    this.status = status;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public WovpPatchStatus getStatus() {
    return status;
  }

  public void setStatus(WovpPatchStatus status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getBackupFile() {
    return backupFile;
  }

  public void setBackupFile(String backupFile) {
    this.backupFile = backupFile;
  }

  @Override
  public String toString() {
    return "WovpPatchResult{gameId=" + gameId + ", status=" + status + ", message='" + message + "'}";
  }
}
