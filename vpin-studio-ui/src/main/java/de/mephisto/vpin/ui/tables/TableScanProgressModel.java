package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class TableScanProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(TableScanProgressModel.class);
  private final Iterator<GameRepresentation> iterator;
  private final List<GameRepresentation> gameRepresentations;

  public TableScanProgressModel(String title, List<GameRepresentation> gameRepresentations) {
    super(title);
    iterator = gameRepresentations.iterator();
    this.gameRepresentations = gameRepresentations;
    Studio.client.getDmdService().clearCache();
  }

  @Override
  public int getMax() {
    return gameRepresentations.size();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public GameRepresentation getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return gameRepresentations.size() == 1;
  }

  @Override
  public String nextToString(GameRepresentation game) {
    return game.getGameDisplayName();
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  public void processNext(ProgressResultModel progressResultModel, GameRepresentation game) {
    try {
      Studio.client.getGameService().scanGame(game.getId());
      progressResultModel.addProcessed();
    } catch (Exception e) {
      LOG.error("Table scan error: " + e.getMessage(), e);
    }
  }
}
