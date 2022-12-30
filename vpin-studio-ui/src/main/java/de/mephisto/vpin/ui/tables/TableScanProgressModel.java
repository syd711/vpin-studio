package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;

public class TableScanProgressModel extends ProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(TableScanProgressModel.class);
  private final Iterator<GameRepresentation> iterator;

  private final VPinStudioClient client;

  public TableScanProgressModel(VPinStudioClient client, String title, GameRepresentation gameRepresentation) {
    super(title);
    this.client = client;
    iterator = Arrays.asList(gameRepresentation).iterator();
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public int getMax() {
    return 1;
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  public String processNext(ProgressResultModel progressResultModel) {
    try {
      GameRepresentation game = iterator.next();
      client.scanGame(game.getId());
      progressResultModel.addProcessed();
      return game.getGameDisplayName();
    } catch (Exception e) {
      LOG.error("Generate card error: " + e.getMessage(), e);
    }
    return null;
  }
}
