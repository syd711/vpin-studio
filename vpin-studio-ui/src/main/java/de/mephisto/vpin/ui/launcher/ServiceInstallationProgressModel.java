package de.mephisto.vpin.ui.launcher;

import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServiceInstallationProgressModel extends ProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(ServiceInstallationProgressModel.class);

  private VPinStudioClient client;
  private Iterator<String> iterator;
  private Iterator<GameRepresentation> gamesIterator;

  private int max;

  private int index = 0;
  private List<GameRepresentation> games;

  public ServiceInstallationProgressModel(VPinStudioClient client) {
    super("Installing VPin Studio Server");
    this.client = client;
    int gameCount = this.client.getGameCount();
    this.max = 1 + gameCount;

    LOG.info(gameCount + " tables found for initial setup.");
    List<String> iteratorList = new ArrayList<>();
    for (int i = 0; i < this.max; i++) {
      iteratorList.add("");
    }
    this.iterator = iteratorList.iterator();
  }

  @Override
  public int getMax() {
    return max;
  }

  @Override
  public Iterator getIterator() {
    return this.iterator;
  }

  @Override
  public String processNext(ProgressResultModel progressResultModel) {
    try {
      if (index == 0) {
        index++;
        iterator.next();
        games = client.getGames();
        gamesIterator = games.iterator();
        progressResultModel.addProcessed();
        return "Preparing initial table scan...";
      }

      iterator.next();
      if (gamesIterator.hasNext()) {
        GameRepresentation next = gamesIterator.next();
        client.scanGame(next);
        progressResultModel.addProcessed();
        return next.getGameDisplayName();
      }
    } catch (Exception e) {
      LOG.error("Generate card error: " + e.getMessage(), e);
    }
    return null;
  }
}
