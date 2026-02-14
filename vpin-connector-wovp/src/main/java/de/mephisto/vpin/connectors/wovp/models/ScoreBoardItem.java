package de.mephisto.vpin.connectors.wovp.models;

public class ScoreBoardItem {
  private int position;
  private ScoreBoardItemPositionValues values;

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public ScoreBoardItemPositionValues getValues() {
    return values;
  }

  public void setValues(ScoreBoardItemPositionValues values) {
    this.values = values;
  }
}
