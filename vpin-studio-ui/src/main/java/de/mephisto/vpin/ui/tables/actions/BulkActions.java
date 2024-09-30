package de.mephisto.vpin.ui.tables.actions;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;

public class BulkActions {

  private final static List<Action> actions = new ArrayList<>();

  static {
    actions.add(new DisableTableAction(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN), "Disabling Tables"));
    actions.add(new EnableTableAction(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN), "Activating Tables"));
  }

  public static boolean consume(List<GameRepresentation> games, KeyEvent event) {
    for (Action action : actions) {
      if (action.matches(event)) {
        ProgressDialog.createProgressDialog(new BulkActionProgressModel(games, action));
        event.consume();
        return true;
      }
    }
    return false;
  }
}
