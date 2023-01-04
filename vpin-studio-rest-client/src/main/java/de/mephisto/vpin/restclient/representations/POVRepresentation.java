package de.mephisto.vpin.restclient.representations;

import java.util.Map;

public class POVRepresentation {
  private int gameId;
  private Map<String, Object> values;

  public POVRepresentation() {

  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public POVRepresentation(Map<String, Object> values) {
    this.values = values;
  }

  public Object getValue(String property) {
    return this.values.get(property);
  }

  public int getIntValue(String property) {
    return (int) this.values.get(property);
  }


  public double getDoubleValue(String property) {
    return (double) this.values.get(property);
  }

  public boolean getBooleanValue(String property) {
    return ((int) this.values.get(property)) == 1;
  }
}
