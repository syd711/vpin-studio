package de.mephisto.vpin.ui.backglassmanager.dialogs;

import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class BackglassBulkImportItem {
  private final int gameId;
  private final String displayName;
  private final DirectB2S backglass;
  private final BooleanProperty selected = new SimpleBooleanProperty(true);

  public BackglassBulkImportItem(int gameId, String displayName, DirectB2S backglass) {
    this.gameId = gameId;
    this.displayName = displayName;
    this.backglass = backglass;
  }

  public int getGameId() {
    return gameId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public DirectB2S getBackglass() {
    return backglass;
  }

  public BooleanProperty selectedProperty() {
    return selected;
  }

  public boolean isSelected() {
    return selected.get();
  }
}
