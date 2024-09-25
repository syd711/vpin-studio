package de.mephisto.vpin.ui.tables.actions;

import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.events.EventManager;
import javafx.scene.input.KeyCombination;

import static de.mephisto.vpin.ui.Studio.client;

public class EnableTableAction extends Action {

  public EnableTableAction(KeyCombination keyCombination, String description) {
    super(keyCombination, description);
  }

  @Override
  public void execute(GameRepresentation game) throws Exception {
    TableDetails tableDetails = client.getFrontendService().getTableDetails(game.getId());
    tableDetails.setStatus(1);
    client.getFrontendService().saveTableDetails(tableDetails, game.getId());
    EventManager.getInstance().notifyTableChange(game.getId(), null);
  }
}
