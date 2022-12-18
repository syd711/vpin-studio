package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class TablesScanProgressModel extends ProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(TablesScanProgressModel.class);
  private final Iterator<Integer> iterator;
  private final List<Integer> gameIds;

  private final VPinStudioClient client;

  public TablesScanProgressModel(VPinStudioClient client, String title) {
    super(title);
    this.client = client;
    this.gameIds = client.getGameIds();
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

  public String processNext(ProgressResultModel progressResultModel) {
    try {
      Integer id = iterator.next();
      GameRepresentation game = client.scanGame(id);
      if(game != null) {
        progressResultModel.addProcessed();
        return game.getGameDisplayName();
      }
      return "Unknown Game";
    } catch (Exception e) {
      LOG.error("Generate card error: " + e.getMessage(), e);
    }
    return null;
  }
}
