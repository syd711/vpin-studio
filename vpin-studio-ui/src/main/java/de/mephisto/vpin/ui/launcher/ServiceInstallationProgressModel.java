package de.mephisto.vpin.ui.launcher;

import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class ServiceInstallationProgressModel extends ProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(ServiceInstallationProgressModel.class);

  private VPinStudioClient client;
  private Iterator<Integer> gamesIterator;

  private int max;
  private boolean hasNext = true;
  private List<Integer> gameIds;

  public ServiceInstallationProgressModel(VPinStudioClient client) {
    super("Initial Table Scan");
    this.client = client;
    int gameCount = this.client.getGameCount();
    this.max = gameCount;
  }

  @Override
  public int getMax() {
    return max;
  }

  @Override
  public boolean hasNext() {
    return hasNext;
  }

  @Override
  public String processNext(ProgressResultModel progressResultModel) {
    try {
      if (gameIds == null) {
        gameIds = client.getGameIds();
        this.max = gameIds.size();
        gamesIterator = gameIds.iterator();
        return "Preparing initial table scan...";
      }

      this.hasNext = gamesIterator.hasNext();
      if (hasNext) {
        Integer next = gamesIterator.next();
        GameRepresentation gameRepresentation = client.scanGame(next);
        progressResultModel.addProcessed();
        return gameRepresentation.getGameDisplayName();
      }
    } catch (Exception e) {
      LOG.error("Generate card error: " + e.getMessage(), e);
    }
    return null;
  }
}
