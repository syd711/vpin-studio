package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
  public int getMax() {
    return 1;
  }

  @Override
  public Iterator<GameRepresentation> getIterator() {
    return iterator;
  }

  public String processNext(ProgressResultModel progressResultModel) {
    try {
      GameRepresentation game = iterator.next();
      boolean result = client.scanGame(game);
      if (result) {
        progressResultModel.addProcessed();
      }
      else {
        progressResultModel.addSkipped();
      }
      return game.getGameDisplayName();
    } catch (Exception e) {
      LOG.error("Generate card error: " + e.getMessage(), e);
    }
    return null;
  }
}
