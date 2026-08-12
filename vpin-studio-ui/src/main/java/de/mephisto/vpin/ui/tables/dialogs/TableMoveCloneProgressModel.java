package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static de.mephisto.vpin.ui.Studio.client;

public class TableMoveCloneProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final GameRepresentation game;
  private final GameEmulatorRepresentation targetEmulator;
  private final boolean move;

  private boolean consumed = false;

  public TableMoveCloneProgressModel(GameRepresentation game, GameEmulatorRepresentation targetEmulator, boolean move) {
    super(move ? "Moving Table" : "Cloning Table");
    this.game = game;
    this.targetEmulator = targetEmulator;
    this.move = move;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public int getMax() {
    return 1;
  }

  @Override
  public boolean hasNext() {
    return !consumed;
  }

  @Override
  public GameRepresentation getNext() {
    consumed = true;
    return game;
  }

  @Override
  public String nextToString(GameRepresentation game) {
    return (move ? "Moving \"" : "Cloning \"") + game.getGameDisplayName() + "\" to \"" + targetEmulator.getName() + "\"";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, GameRepresentation game) {
    try {
      client.getGameService().moveOrCloneGame(game.getId(), targetEmulator.getId(), move);
      progressResultModel.addProcessed(game.getId());
    }
    catch (Exception e) {
      progressResultModel.addError();
      LOG.error("Failed to {} table \"{}\": {}", move ? "move" : "clone", game.getGameDisplayName(), e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to " + (move ? "move" : "clone") + " table \"" + game.getGameDisplayName() + "\": " + e.getMessage());
      });
    }
  }
}
