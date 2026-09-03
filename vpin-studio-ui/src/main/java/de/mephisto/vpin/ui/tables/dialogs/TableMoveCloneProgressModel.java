package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.SubfolderNaming;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class TableMoveCloneProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final List<GameRepresentation> games;
  private final Iterator<GameRepresentation> gameIterator;
  private final GameEmulatorRepresentation targetEmulator;
  private final boolean move;
  private final boolean createSubfolder;
  private final SubfolderNaming subfolderNaming;

  public TableMoveCloneProgressModel(List<GameRepresentation> games, GameEmulatorRepresentation targetEmulator, boolean move, boolean createSubfolder, SubfolderNaming subfolderNaming) {
    super(move ? Messages.get("dialog.moving_table") : Messages.get("dialog.cloning_table_title"));
    this.games = games;
    this.gameIterator = games.iterator();
    this.targetEmulator = targetEmulator;
    this.move = move;
    this.createSubfolder = createSubfolder;
    this.subfolderNaming = subfolderNaming;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public boolean isIndeterminate() {
    return games.size() == 1;
  }

  @Override
  public int getMax() {
    return games.size();
  }

  @Override
  public boolean hasNext() {
    return gameIterator.hasNext();
  }

  @Override
  public GameRepresentation getNext() {
    return gameIterator.next();
  }

  @Override
  public String nextToString(GameRepresentation game) {
    return move ? Messages.get("dialog.moving_to", game.getGameDisplayName(), targetEmulator.getName())
        : Messages.get("dialog.cloning_to", game.getGameDisplayName(), targetEmulator.getName());
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, GameRepresentation game) {
    try {
      client.getGameService().moveOrCloneGame(game.getId(), targetEmulator.getId(), move, createSubfolder, subfolderNaming);
      progressResultModel.addProcessed(game.getId());
    }
    catch (Exception e) {
      progressResultModel.addError();
      LOG.error("Failed to {} table \"{}\": {}", move ? "move" : "clone", game.getGameDisplayName(), e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("dialog.failed_to") + (move ? "move" : "clone") + Messages.get("dialog.table_2") + game.getGameDisplayName() + Messages.get("dialog.item") + e.getMessage());
      });
    }
  }
}
