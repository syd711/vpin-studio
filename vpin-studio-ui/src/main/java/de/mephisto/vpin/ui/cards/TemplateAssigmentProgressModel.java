package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.commons.fx.pausemenu.PauseMenuUIDefaults.MAX_REFRESH_COUNT;
import static de.mephisto.vpin.ui.Studio.client;

public class TemplateAssigmentProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(TemplateAssigmentProgressModel.class);
  private List<GameRepresentation> games;

  private final Iterator<GameRepresentation> gameIterator;
  private final long templateId;

  public TemplateAssigmentProgressModel(List<GameRepresentation> games, long templateId) {
    super("Applying Template");
    this.games = games;
    this.gameIterator = games.iterator();
    this.templateId = templateId;
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
  public void finalizeModel(ProgressResultModel progressResultModel) {
    super.finalizeModel(progressResultModel);

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
  public void processNext(ProgressResultModel progressResultModel, GameRepresentation game) {
    game.setTemplateId(templateId);
    try {
      client.getGameService().saveGame(game);
    }
    catch (Exception e) {
      LOG.error("Failed to save template mapping: {}", e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save template mapping: " + e.getMessage());
      });
    }
  }
}
