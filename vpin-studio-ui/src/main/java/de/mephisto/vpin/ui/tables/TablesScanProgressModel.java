package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class TablesScanProgressModel extends ProgressModel<Integer> {
  private final static Logger LOG = LoggerFactory.getLogger(TablesScanProgressModel.class);
  private final Iterator<Integer> iterator;
  private final List<Integer> gameIds;

  public TablesScanProgressModel(String title) {
    super(title);
    this.gameIds = Studio.client.getGameIds();
    iterator = gameIds.iterator();
  }

  @Override
  public int getMax() {
    return gameIds.size();
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  @Override
  public Integer getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(Integer id) {
    return null;
  }

  public void processNext(ProgressResultModel progressResultModel, Integer id) {
    try {
      GameRepresentation game = Studio.client.scanGame(id);
      if (game != null) {
        progressResultModel.addProcessed();
      }
    } catch (Exception e) {
      LOG.error("Table scan error: " + e.getMessage(), e);
    }
  }
}
