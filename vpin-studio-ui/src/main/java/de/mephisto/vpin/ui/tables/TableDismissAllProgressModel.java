package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.DismissalUtil;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.commons.fx.pausemenu.PauseMenuUIDefaults.MAX_REFRESH_COUNT;

public class TableDismissAllProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(TableDismissAllProgressModel.class);
  private List<GameRepresentation> games;

  private final Iterator<GameRepresentation> gameIterator;

  public TableDismissAllProgressModel(List<GameRepresentation> games) {
    super("Dismissing Table Validation Errors");
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
    try {
      List<ValidationState> validations = Studio.client.getGameService().getValidations(game.getId());
      DismissalUtil.dismissSelection(game, validations.stream().map(v -> v.getCode()).collect(Collectors.toList()));
    }
    catch (Exception e) {
      LOG.error("Error during dismissal: " + e.getMessage(), e);
    }
  }
}
