package de.mephisto.vpin.ui.tables.actions;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;

public class BulkActionProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private List<GameRepresentation> games;

  private final Iterator<GameRepresentation> gameIterator;
  private final Action action;

  public BulkActionProgressModel(List<GameRepresentation> games, Action action) {
    super(action.getDescription());
    this.games = games;
    this.gameIterator = games.iterator();
    this.action = action;
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
  public void processNext(ProgressResultModel progressResultModel, GameRepresentation game) {
    try {
      action.execute(game);
    }
    catch (Exception e) {
      progressResultModel.getResults().add(e.getMessage());
    }
  }
}
