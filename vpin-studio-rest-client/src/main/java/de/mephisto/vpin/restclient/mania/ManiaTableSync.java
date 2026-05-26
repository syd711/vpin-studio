package de.mephisto.vpin.restclient.mania;

import de.mephisto.vpin.connectors.mania.model.TableScore;
import org.jspecify.annotations.Nullable;

public class ManiaTableSync {
  @Nullable
  private String result = null;

  @Nullable
  private TableScore tableScore;

  private String tableName;
  private String tableType;
  private boolean denied = false;
  private boolean valid = true;
  private int gameId;

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public String getTableType() {
    return tableType;
  }

  public void setTableType(String tableType) {
    this.tableType = tableType;
  }

  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public boolean isDenied() {
    return denied;
  }

  public void setDenied(boolean denied) {
    this.denied = denied;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public TableScore getTableScore() {
    return tableScore;
  }

  public void setTableScore(TableScore tableScore) {
    this.tableScore = tableScore;
  }
}
