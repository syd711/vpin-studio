package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.games.GameList;
import de.mephisto.vpin.restclient.games.GameListItem;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;

import static de.mephisto.vpin.ui.Studio.client;

public class TableImportProgressModel extends ProgressModel<GameListItem> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Iterator<GameListItem> iterator;
  private final GameList gameList;

  public TableImportProgressModel(GameList gameList) {
    super("Importing Tables");
    this.gameList = gameList;
    iterator = this.gameList.getItems().iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return this.gameList.getItems().size();
  }

  @Override
  public GameListItem getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(GameListItem item) {
    return "Importing " + item.getName();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, GameListItem next) {
    try {
      JobDescriptor jobExecutionResult = client.getFrontendService().importTable(next);
      progressResultModel.getResults().add(jobExecutionResult);
      LOG.info("Import finished: \"" + next.getName() + "\"");
    } catch (Exception e) {
      LOG.error("Table import failed: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
