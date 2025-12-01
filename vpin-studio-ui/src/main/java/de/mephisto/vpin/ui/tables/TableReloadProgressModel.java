package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class TableReloadProgressModel extends ProgressModel<Integer> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private List<Integer> gameIds;

  private Iterator<Integer> gameIdIterator;
  private String lastScannedName = null;

  public TableReloadProgressModel(List<Integer> gameIds) {
    super("Scanning New Tables");
    this.gameIdIterator = gameIds.iterator();
    this.gameIds = gameIds;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return gameIds.size();
  }

  @Override
  public boolean hasNext() {
    return this.gameIdIterator.hasNext();
  }

  @Override
  public Integer getNext() {
    return gameIdIterator.next();
  }

  @Override
  public String nextToString(Integer id) {
    if (lastScannedName != null) {
      return "Scanned \"" + lastScannedName + "\"";
    }
    return null;
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    client.getGameService().clearCache();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, Integer next) {
    try {
      GameRepresentation gameRepresentation = client.getGame(next);
      lastScannedName = null;
      if (gameRepresentation != null) {
        lastScannedName = gameRepresentation.getGameDisplayName();
        client.getGameService().scanGameScore(gameRepresentation.getId());
      }
      progressResultModel.addProcessed();
    } catch (Exception e) {
      LOG.error("Error during service installation: " + e.getMessage(), e);
    }
  }


}
