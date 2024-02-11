package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class TableActions {

  public static void onVpsReset(List<GameRepresentation> selectedItems) {
    try {
      List<GameRepresentation> collect = selectedItems.stream().filter(g -> !g.getUpdates().isEmpty()).collect(Collectors.toList());
      for (GameRepresentation gameRepresentation : collect) {
        gameRepresentation.setUpdates(Collections.emptyList());
        client.getGameService().saveGame(gameRepresentation);
        EventManager.getInstance().notifyTableChange(gameRepresentation.getId(), null);
      }
    } catch (Exception e) {
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to reset VPS updates information: " + e.getMessage());
    }
  }

}
