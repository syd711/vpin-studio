package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.games.GameList;
import de.mephisto.vpin.restclient.games.GameListItem;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadDescriptor;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class TableImportProgressModel extends ProgressModel<GameListItem> {
  private final static Logger LOG = LoggerFactory.getLogger(TableImportProgressModel.class);

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
      JobExecutionResult jobExecutionResult = client.getPinUPPopperService().importTable(next);
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
