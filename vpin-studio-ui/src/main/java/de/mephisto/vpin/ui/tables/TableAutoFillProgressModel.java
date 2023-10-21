package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class TableAutoFillProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(TableAutoFillProgressModel.class);
  private List<GameRepresentation> games;

  private final boolean overwrite;
  private Iterator<GameRepresentation> gameIterator;

  public TableAutoFillProgressModel(List<GameRepresentation> games, boolean overwrite) {
    super("Auto-Fill Table Data");
    this.overwrite = overwrite;
    this.games = games;
    this.gameIterator = games.iterator();
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
  public void processNext(ProgressResultModel progressResultModel, GameRepresentation next) {
    try {
      client.getPinUPPopperService().autoFillTableDetails(next.getId(), overwrite);
      progressResultModel.addProcessed();
    } catch (Exception e) {
      LOG.error("Error auto-filling table data: " + e.getMessage(), e);
    }
  }
}
