package de.mephisto.vpin.ui.launcher;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class ServiceInstallationProgressModel extends ProgressModel<Integer> {
  private final static Logger LOG = LoggerFactory.getLogger(ServiceInstallationProgressModel.class);
  private final List<Integer> gameIds;

  private Iterator<Integer> gameIdIterator;
  private String lastScannedName = null;

  public ServiceInstallationProgressModel(VPinStudioClient client) {
    super("Initial Table Scan");
    gameIds = client.getGameService().getGameIds();
    gameIdIterator = gameIds.iterator();
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
  public boolean isShowSummary() {
    return false;
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
    Studio.client.getMameService().clearCache();
    super.finalizeModel(progressResultModel);
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, Integer next) {
    try {
      GameRepresentation gameRepresentation = Studio.client.getGameService().scanGame(next);
      lastScannedName = null;
      if (gameRepresentation != null) {
        lastScannedName = gameRepresentation.getGameDisplayName();
      }
      progressResultModel.addProcessed();
    }
    catch (Exception e) {
      LOG.error("Error during service installation: " + e.getMessage(), e);
    }
  }
}
