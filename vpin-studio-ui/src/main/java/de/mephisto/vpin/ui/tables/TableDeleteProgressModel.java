package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.DeleteDescriptor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class TableDeleteProgressModel extends ProgressModel<Integer> {
  private final static Logger LOG = LoggerFactory.getLogger(TableDeleteProgressModel.class);
  private final TableOverviewController tableOverviewController;
  private final List<Integer> games;
  private final DeleteDescriptor descriptor;

  private final Iterator<Integer> gameIterator;

  public TableDeleteProgressModel(TableOverviewController tableOverviewController, DeleteDescriptor descriptor) {
    super("Table Deletion");
    this.tableOverviewController = tableOverviewController;
    this.games = descriptor.getGameIds();
    this.descriptor = descriptor;
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
  public Integer getNext() {
    return gameIterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return this.games.size() == 1;
  }

  @Override
  public String nextToString(Integer game) {
    return "Deleting Table";
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    super.finalizeModel(progressResultModel);

    tableOverviewController.doReload(false);
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, Integer gameId) {
    try {
      GameRepresentation game = client.getGameService().getGame(gameId);
      descriptor.setGameIds(Arrays.asList(gameId));
      client.getGameService().deleteGame(descriptor, game);
    }
    catch (Exception e) {
      LOG.error("Error during dismissal: " + e.getMessage(), e);
    }
  }
}
