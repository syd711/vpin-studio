package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.commons.fx.pausemenu.PauseMenuUIDefaults.MAX_REFRESH_COUNT;

public class TableStatusToggleProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(TableStatusToggleProgressModel.class);
  private final List<GameRepresentation> games;
  private final List<GameRepresentation> failed = new ArrayList<>();

  private final Iterator<GameRepresentation> gameIterator;

  public TableStatusToggleProgressModel(List<GameRepresentation> games) {
    super(Messages.get("dialog.updating_table_status"));
    this.games = games;
    this.gameIterator = games.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    if (games.size() > MAX_REFRESH_COUNT) {
      EventManager.getInstance().notifyTablesChanged();
    }
    else {
      for (GameRepresentation game : games) {
        EventManager.getInstance().notifyTableChange(game.getId(), null);
      }
    }

    if (!failed.isEmpty()) {
      GameRepresentation game = failed.getFirst();
      String action = game.isDisabled() ? "enabled" : "disabled";
      WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.the_table") + game.getGameDisplayName() + Messages.get("dialog.couldn_t_be") + action + ".", "Please try again.");
    }
  }

  @Override
  public int getMax() {
    return games.size();
  }

  @Override
  public boolean hasNext() {
    return this.gameIterator.hasNext();
  }

  @Override
  public GameRepresentation getNext() {
    return gameIterator.next();
  }

  @Override
  public String nextToString(GameRepresentation game) {
    return game.getGameDisplayName();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, GameRepresentation game) {
    boolean isDisable = game.isDisabled();
    try {
      TableDetails detail = Studio.client.getFrontendService().getTableDetails(game.getId());
      detail.setStatus(isDisable ? 1 : 0);
      Studio.client.getFrontendService().saveTableDetails(detail, game.getId());
      progressResultModel.addProcessed();
    }
    catch (Exception e) {
      LOG.error("Cannot " + (isDisable ? "enable" : "disable") + " the game " + game.getGameFileName(), e);
      failed.add(game);
      progressResultModel.addError();
    }
  }
}
