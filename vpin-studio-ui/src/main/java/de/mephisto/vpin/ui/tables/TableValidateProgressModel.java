package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class TableValidateProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private List<GameRepresentation> games;

  private final Iterator<GameRepresentation> gameIterator;
  private final boolean reload;

  public TableValidateProgressModel(String title, List<GameRepresentation> games, boolean reload) {
    super(title);
    this.games = games;
    this.gameIterator = games.iterator();
    this.reload = reload;
  }

  @Override
  public boolean isShowSummary() {
    return false;
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
      game.setIgnoredValidations(Collections.emptyList());

      try {
        client.getGameService().saveGame(game);
        if (!reload) {
          EventManager.getInstance().notifyTableChange(game.getId(), null);
        }
      } catch (Exception e) {
        LOG.error("Table validation failed: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Table validation failed: " + e.getMessage());
      }
    } catch (Exception e) {
      LOG.error("Error validating table: " + e.getMessage(), e);
    }
  }
}
